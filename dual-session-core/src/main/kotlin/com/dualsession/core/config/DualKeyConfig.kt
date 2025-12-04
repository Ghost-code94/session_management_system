package com.dualsession.core.config

enum class SameSite { Lax, Strict, None }

data class DualKeyConfig(
    val sessionTtlSeconds: Int = 3600,
    val sessionCookieName: String = "auth_session_cookie",
    val csrfCookieName: String = "csrf_token",
    val cookieDomain: String = "",
    val secure: Boolean = true,
    val sameSiteSession: SameSite = SameSite.None,
    val sameSiteCsrf: SameSite = SameSite.Lax,
    val cookiePath: String = "/"
)
