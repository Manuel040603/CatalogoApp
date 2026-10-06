package com.catalogoapp.consultoras.data.model

data class Producto @JvmOverloads constructor(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val precio: Double = 0.0,
    val descripcion: String = "",
    val stock: Int = 0,
    val imagenUrl: String = ""
)
