package com.dualsession.core.crypto

import java.security.SecureRandom
import java.util.Base64

object CsrfTokenGenerator {
    private val secureRandom = SecureRandom()

    fun generateCsrfToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
