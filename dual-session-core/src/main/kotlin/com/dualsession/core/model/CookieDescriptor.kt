package com.dualsession.core.model

import com.dualsession.core.config.SameSite

data class CookieDescriptor(
    val name: String,
    val value: String,
    val domain: String,
    val path: String = "/",
    val maxAgeSeconds: Int,
    val httpOnly: Boolean,
    val secure: Boolean,
    val sameSite: SameSite
)
