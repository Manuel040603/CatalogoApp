package com.catalogoapp.consultoras.data.model

data class ConsultoraProfile @JvmOverloads constructor(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val preferencias: String = "",
    val tienePhotoLocal: Boolean = false,
    val activo: Boolean = true,
    val ultimaActualizacion: Long = System.currentTimeMillis()
)