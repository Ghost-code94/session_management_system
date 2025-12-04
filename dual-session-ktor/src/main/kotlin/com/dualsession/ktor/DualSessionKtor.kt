package com.dualsession.ktor

import com.dualsession.core.config.DualKeyConfig
import com.dualsession.core.engine.DualKeySessionManager
import com.dualsession.core.model.CookieDescriptor
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.AttributeKey
import org.slf4j.LoggerFactory
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Install pre-routing session reconstruction.
 *
 * - Reads session cookie (from DualKeyConfig)
 * - Loads session via DualKeySessionManager
 * - Attaches session to ApplicationCall.attributes under sessionKey
 */
fun <TSession : Any> Application.installDualKeySession(
    manager: DualKeySessionManager<TSession>,
    config: DualKeyConfig,
    sessionKey: AttributeKey<TSession>
) {
    val log = LoggerFactory.getLogger("DualKeySession")

    intercept(ApplicationCallPipeline.Features) {
        val sid = call.request.cookies[config.sessionCookieName]
        if (sid == null) {
            proceed()
            return@intercept
        }

        val session = manager.loadSession(sid)
        if (session == null) {
            // Cookie present but no record: expire cookie
            call.response.cookies.append(
                name = config.sessionCookieName,
                value = "",
                domain = config.cookieDomain,
                path = config.cookiePath,
                maxAge = 0,
                httpOnly = true,
                secure = config.secure,
                extensions = mapOf("SameSite" to config.sameSiteSession.name)
            )
            proceed()
            return@intercept
        }

        log.info("Session $sid loaded")
        call.attributes.put(sessionKey, session)
        proceed()
    }
}

/**
 * Helper: apply a CookieDescriptor to Ktor's response cookies.
 */
private fun CookieDescriptor.applyTo(response: ApplicationResponse) {
    response.cookies.append(
        name = name,
        value = value,
        domain = domain,
        path = path,
        maxAge = maxAgeSeconds,
        httpOnly = httpOnly,
        secure = secure,
        extensions = mapOf("SameSite" to sameSite.name)
    )
}

/**
 * Create/refresh dual-key session and set cookies.
 *
 * - Clears old session if present
 * - Creates new session+CSRF via manager
 * - Sets session + CSRF cookies using descriptors
 */
suspend fun <TSession : Any> ApplicationCall.setDualSession(
    manager: DualKeySessionManager<TSession>,
    config: DualKeyConfig,
    session: TSession
) {
    val oldSid = request.cookies[config.sessionCookieName]
    if (oldSid != null) {
        manager.clearSession(oldSid)
    }

    val result = manager.createSession(session)

    // Apply cookies to response
    result.sessionCookie.applyTo(response)
    result.csrfCookie.applyTo(response)
}

/**
 * Clear session:
 * - Remove record from storage via manager
 * - Expire both session and CSRF cookies
 */
suspend fun <TSession : Any> ApplicationCall.clearDualSession(
    manager: DualKeySessionManager<TSession>,
    config: DualKeyConfig
) {
    val sid = request.cookies[config.sessionCookieName]
    if (sid != null) {
        manager.clearSession(sid)
    }

    response.cookies.append(
        name = config.sessionCookieName,
        value = "",
        domain = config.cookieDomain,
        path = config.cookiePath,
        maxAge = 0,
        httpOnly = true,
        secure = config.secure,
        extensions = mapOf("SameSite" to config.sameSiteSession.name)
    )

    response.cookies.append(
        name = config.csrfCookieName,
        value = "",
        domain = config.cookieDomain,
        path = config.cookiePath,
        maxAge = 0,
        httpOnly = false,
        secure = config.secure,
        extensions = mapOf("SameSite" to config.sameSiteCsrf.name)
    )
}

/**
 * Install CSRF guard.
 *
 * - Skips safe methods (GET/HEAD/OPTIONS)
 * - Skips paths in exemptPaths
 * - Requires session to be present (via sessionKey)
 * - Validates CSRF using manager (cookie + header + stored)
 */
fun <TSession : Any> Application.installCsrfGuard(
    manager: DualKeySessionManager<TSession>,
    config: DualKeyConfig,
    sessionKey: AttributeKey<TSession>,
    exemptPaths: Set<String> = setOf("/api/login", "/api/logout"),
    csrfHeaderName: String = "X-CSRF"
) {
    intercept(ApplicationCallPipeline.Features) {
        val method = call.request.local.method

        if (method !in setOf(HttpMethod.Get, HttpMethod.Head, HttpMethod.Options)) {
            val path = call.request.path()

            if (path !in exemptPaths) {
                val user = if (call.attributes.contains(sessionKey)) {
                    call.attributes[sessionKey]
                } else {
                    null
                }
                if (user != null) {
                    fun dec(s: String?) =
                        s?.let { URLDecoder.decode(it, StandardCharsets.UTF_8) }

                    val sid = call.request.cookies[config.sessionCookieName]
                    val cTok = dec(call.request.cookies[config.csrfCookieName])
                    val hTok = dec(call.request.headers[csrfHeaderName]
                        ?: call.request.headers["X-CSRF"])

                    val ok = manager.validateCsrf(
                        sessionId = sid,
                        cookieToken = cTok,
                        headerToken = hTok
                    )

                    if (!ok) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            mapOf("error" to "CSRF validation failed")
                        )
                        return@intercept
                    }
                }
            }
        }

        proceed()
    }
}
