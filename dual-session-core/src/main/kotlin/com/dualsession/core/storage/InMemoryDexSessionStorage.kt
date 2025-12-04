package com.dualsession.core.storage

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryDexSessionStorage : DexSessionStorage {
    private val mutex = Mutex()
    private val map = mutableMapOf<String, ByteArray>()

    override suspend fun write(id: String, data: ByteArray) {
        mutex.withLock {
            map[id] = data
        }
    }

    override suspend fun read(id: String): ByteArray? = mutex.withLock {
        map[id]
    }

    override suspend fun invalidate(id: String) {
        mutex.withLock {
            map.remove(id)
        }
    }
}
