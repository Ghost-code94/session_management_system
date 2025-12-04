package com.dualsession.core.storage

interface DexSessionStorage {
    suspend fun write(id: String, data: ByteArray)
    suspend fun read(id: String): ByteArray?
    suspend fun invalidate(id: String)
}
