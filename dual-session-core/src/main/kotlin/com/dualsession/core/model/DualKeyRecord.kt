package com.dualsession.core.model

data class DualKeyRecord<TSession>(
    val sessionId: String,
    val csrfToken: String,
    val session: TSession,
    val expiresAtEpochSeconds: Long
)
