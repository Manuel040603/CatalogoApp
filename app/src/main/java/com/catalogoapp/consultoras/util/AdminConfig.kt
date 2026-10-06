package com.catalogoapp.consultoras.util

object AdminConfig {
    private val CORREOS_ADMIN = setOf("admin@catalogoapp.com")

    fun esAdmin(email: String?): Boolean =
        email != null && CORREOS_ADMIN.contains(email.trim().lowercase())
}
