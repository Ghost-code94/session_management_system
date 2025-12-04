package com.dualsession.core.engine

import com.google.gson.Gson
import com.dualsession.core.model.DualKeyRecord

class JsonSessionSerializer<TSession>(
    private val gson: Gson = Gson(),
    private val sessionClass: Class<TSession>
) {

    fun serializeRecord(record: DualKeyRecord<TSession>): ByteArray =
        gson.toJson(record).toByteArray(Charsets.UTF_8)

    fun deserializeRecord(bytes: ByteArray?): DualKeyRecord<TSession>? {
        if (bytes == null || bytes.isEmpty()) return null
        val json = bytes.toString(Charsets.UTF_8)
        return runCatching {
            val type = com.google.gson.reflect.TypeToken
                .getParameterized(DualKeyRecord::class.java, sessionClass)
                .type
            gson.fromJson<DualKeyRecord<TSession>>(json, type)
        }.getOrNull()
    }
}
