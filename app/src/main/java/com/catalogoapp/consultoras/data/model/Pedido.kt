package com.catalogoapp.consultoras.data.model

data class ItemPedidoDto(
    val productoId: String = "",
    val nombreProducto: String = "",
    val cantidad: Int = 0,
    val precioUnitario: Double = 0.0
)

data class CrearPedidoRequest(
    val nombreCliente: String,
    val telefonoCliente: String,
    val direccion: String = "",
    val items: List<ItemPedidoDto>
)

data class PedidoResponse(
    val id: Int = 0,
    val uidConsultora: String = "",
    val nombreCliente: String = "",
    val telefonoCliente: String = "",
    val direccion: String = "",
    val total: Double = 0.0,
    val estado: String = "PENDIENTE",
    val fechaCreacion: String = "",
    val fechaActualizacion: String = "",
    val items: List<ItemPedidoDto> = emptyList()
)

data class CambiarEstadoRequest(
    val nuevoEstado: String
)

data class RegistrarDispositivoRequest(
    val tokenFcm: String
)

data class CrearRepartoRequest(
    val zona: String,
    val chofer: String,
    val vehiculo: String,
    val direccionOrigen: String,
    val pedidoIds: List<Int>
)

data class ParadaRepartoDto(
    val id: Int = 0,
    val pedidoId: Int = 0,
    val nombreCliente: String = "",
    val direccion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val orden: Int = 0,
    val entregada: Boolean = false
)

data class RepartoResponse(
    val id: Int = 0,
    val zona: String = "",
    val chofer: String = "",
    val vehiculo: String = "",
    val estado: String = "",
    val paradas: List<ParadaRepartoDto> = emptyList(),
    val geometriaRuta: String? = null
)

data class ActualizarUbicacionRequest(
    val latitud: Double,
    val longitud: Double
)

data class ResumenRepartoDto(
    val id: Int = 0,
    val zona: String = "",
    val chofer: String = "",
    val vehiculo: String = "",
    val estado: String = "",
    val fechaCreacion: String = "",
    val totalParadas: Int = 0,
    val paradasEntregadas: Int = 0
)
