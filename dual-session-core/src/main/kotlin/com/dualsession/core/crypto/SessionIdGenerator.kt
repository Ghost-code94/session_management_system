package com.dualsession.core.crypto

import java.security.SecureRandom
import java.util.Base64

object SessionIdGenerator {
    private val secureRandom = SecureRandom()

    fun generateSessionId(): String {
        val randomBytes = ByteArray(32) // 256 bits
        secureRandom.nextBytes(randomBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
    }
}
