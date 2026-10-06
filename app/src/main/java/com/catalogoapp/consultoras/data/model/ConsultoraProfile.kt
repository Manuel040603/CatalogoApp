package com.catalogoapp.consultoras.data.model

data class ConsultoraProfile @JvmOverloads constructor(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val preferencias: String = "",
    val fotoBase64: String = "",
    val activo: Boolean = true,
    val esAdmin: Boolean = false,
    val ultimaActualizacion: Long = System.currentTimeMillis()
)
