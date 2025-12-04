package com.dualsession.core.engine

import com.dualsession.core.model.DualKeyResult

interface DualKeySessionManager<TSession> {
    suspend fun createSession(session: TSession): DualKeyResult
    suspend fun loadSession(sessionId: String?): TSession?
    suspend fun validateCsrf(
        sessionId: String?,
        cookieToken: String?,
        headerToken: String?
    ): Boolean
    suspend fun clearSession(sessionId: String?)
}
