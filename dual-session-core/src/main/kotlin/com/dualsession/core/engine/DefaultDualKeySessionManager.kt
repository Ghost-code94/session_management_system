package com.dualsession.core.engine

import com.dualsession.core.config.DualKeyConfig
import com.dualsession.core.crypto.CsrfTokenGenerator
import com.dualsession.core.crypto.SessionIdGenerator
import com.dualsession.core.crypto.ctEquals
import com.dualsession.core.model.CookieDescriptor
import com.dualsession.core.model.DualKeyRecord
import com.dualsession.core.model.DualKeyResult
import com.dualsession.core.storage.DexSessionStorage

class DefaultDualKeySessionManager<TSession>(
    private val storage: DexSessionStorage,
    private val config: DualKeyConfig,
    private val serializer: JsonSessionSerializer<TSession>,
    private val clockSeconds: () -> Long = { System.currentTimeMillis() / 1000 }
) : DualKeySessionManager<TSession> {

    private fun keyForSession(sid: String) = "sess:$sid"

    override suspend fun createSession(session: TSession): DualKeyResult {
        val sid = SessionIdGenerator.generateSessionId()
        val csrf = CsrfTokenGenerator.generateCsrfToken()

        val record = DualKeyRecord(
            sessionId = sid,
            csrfToken = csrf,
            session = session,
            expiresAtEpochSeconds = clockSeconds() + config.sessionTtlSeconds
        )

        storage.write(keyForSession(sid), serializer.serializeRecord(record))

        val sessionCookie = CookieDescriptor(
            name          = config.sessionCookieName,
            value         = sid,
            domain        = config.cookieDomain,
            path          = config.cookiePath,
            maxAgeSeconds = config.sessionTtlSeconds,
            httpOnly      = true,
            secure        = config.secure,
            sameSite      = config.sameSiteSession
        )

        val csrfCookie = CookieDescriptor(
            name          = config.csrfCookieName,
            value         = csrf,
            domain        = config.cookieDomain,
            path          = config.cookiePath,
            maxAgeSeconds = config.sessionTtlSeconds,
            httpOnly      = false,
            secure        = config.secure,
            sameSite      = config.sameSiteCsrf
        )

        return DualKeyResult(
            sessionId = sid,
            csrfToken = csrf,
            sessionCookie = sessionCookie,
            csrfCookie = csrfCookie
        )
    }

    override suspend fun loadSession(sessionId: String?): TSession? {
        sessionId ?: return null
        val bytes = storage.read(keyForSession(sessionId)) ?: return null
        val record = serializer.deserializeRecord(bytes) ?: return null

        if (record.expiresAtEpochSeconds < clockSeconds()) {
            storage.invalidate(keyForSession(sessionId))
            return null
        }
        return record.session
    }

    override suspend fun validateCsrf(
        sessionId: String?,
        cookieToken: String?,
        headerToken: String?
    ): Boolean {
        if (sessionId == null || cookieToken == null || headerToken == null) return false

        val bytes = storage.read(keyForSession(sessionId)) ?: return false
        val record = serializer.deserializeRecord(bytes) ?: return false
        val expected = record.csrfToken

        return ctEquals(cookieToken, headerToken) &&
               ctEquals(cookieToken, expected)
    }

    override suspend fun clearSession(sessionId: String?) {
        sessionId ?: return
        storage.invalidate(keyForSession(sessionId))
    }
}
