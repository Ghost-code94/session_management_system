package com.dualsession.core.model

data class DualKeyResult(
    val sessionId: String,
    val csrfToken: String,
    val sessionCookie: CookieDescriptor,
    val csrfCookie: CookieDescriptor
)
