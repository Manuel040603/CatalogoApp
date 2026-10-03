package com.catalogoapp.consultoras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalogoapp.consultoras.data.model.CrearPedidoRequest
import com.catalogoapp.consultoras.data.model.ItemPedidoDto
import com.catalogoapp.consultoras.data.model.PedidoResponse
import com.catalogoapp.consultoras.data.model.Producto
import com.catalogoapp.consultoras.data.repository.PedidoRepository
import com.catalogoapp.consultoras.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class ItemCarrito(
    val producto: Producto,
    val cantidad: Int
)

data class PedidoUiState(
    val cargandoLista: Boolean = true,
    val pedidos: List<PedidoResponse> = emptyList(),
    val productosDisponibles: List<Producto> = emptyList(),
    val nombreCliente: String = "",
    val telefonoCliente: String = "",
    val direccion: String = "",
    val carrito: List<ItemCarrito> = emptyList(),
    val guardando: Boolean = false,
    val actualizandoId: Int? = null,
    val mensaje: String? = null,
    val error: String? = null
)

class PedidoViewModel @JvmOverloads constructor(
    private val repository: PedidoRepository = PedidoRepository(),
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PedidoUiState())
    val uiState: StateFlow<PedidoUiState> = _uiState

    init {
        viewModelScope.launch {
            productRepository.observarCatalogo()
                .catch { }
                .collect { productos ->
                    _uiState.value = _uiState.value.copy(productosDisponibles = productos)
                }
        }
        cargarPedidos()
    }

    fun cargarPedidos() {
        _uiState.value = _uiState.value.copy(cargandoLista = true)
        viewModelScope.launch {
            val resultado = repository.listarPedidos()
            _uiState.value = resultado.fold(
                onSuccess = { lista -> _uiState.value.copy(cargandoLista = false, pedidos = lista, error = null) },
                onFailure = { excepcion -> _uiState.value.copy(cargandoLista = false, error = excepcion.message) }
            )
        }
    }

    fun actualizarNombreCliente(valor: String) {
        _uiState.value = _uiState.value.copy(nombreCliente = valor)
    }

    fun actualizarTelefonoCliente(valor: String) {
        _uiState.value = _uiState.value.copy(telefonoCliente = valor)
    }

    fun actualizarDireccion(valor: String) {
        _uiState.value = _uiState.value.copy(direccion = valor)
    }

    fun cambiarCantidadEnCarrito(producto: Producto, cantidad: Int) {
        val actuales = _uiState.value.carrito.toMutableList()
        val indiceExistente = actuales.indexOfFirst { it.producto.id == producto.id }
        if (cantidad <= 0) {
            if (indiceExistente >= 0) actuales.removeAt(indiceExistente)
        } else if (indiceExistente >= 0) {
            actuales[indiceExistente] = actuales[indiceExistente].copy(cantidad = cantidad)
        } else {
            actuales.add(ItemCarrito(producto, cantidad))
        }
        _uiState.value = _uiState.value.copy(carrito = actuales)
    }

    fun cantidadEnCarritoDe(productoId: String): Int {
        return _uiState.value.carrito.firstOrNull { it.producto.id == productoId }?.cantidad ?: 0
    }

    fun totalCarrito(): Double {
        return _uiState.value.carrito.sumOf { it.cantidad * it.producto.precio }
    }

    fun formularioValido(): Boolean {
        val estado = _uiState.value
        return estado.nombreCliente.isNotBlank() && estado.telefonoCliente.isNotBlank() && estado.carrito.isNotEmpty()
    }

    fun crearPedido() {
        val estado = _uiState.value
        if (!formularioValido()) {
            _uiState.value = estado.copy(error = "Completa el cliente, su telefono y agrega al menos un producto")
            return
        }
        _uiState.value = estado.copy(guardando = true, error = null)
        viewModelScope.launch {
            val items = estado.carrito.map { item ->
                ItemPedidoDto(
                    productoId = item.producto.id,
                    nombreProducto = item.producto.nombre,
                    cantidad = item.cantidad,
                    precioUnitario = item.producto.precio
                )
            }
            val datos = CrearPedidoRequest(
                nombreCliente = estado.nombreCliente,
                telefonoCliente = estado.telefonoCliente,
                direccion = estado.direccion,
                items = items
            )
            val resultado = repository.crearPedido(datos)
            _uiState.value = resultado.fold(
                onSuccess = {
                    PedidoUiState(
                        cargandoLista = false,
                        pedidos = listOf(it) + _uiState.value.pedidos,
                        productosDisponibles = _uiState.value.productosDisponibles,
                        mensaje = "Pedido registrado correctamente"
                    )
                },
                onFailure = { excepcion -> _uiState.value.copy(guardando = false, error = excepcion.message) }
            )
        }
    }

    fun siguienteEstadoDe(estadoActual: String): String? {
        return when (estadoActual) {
            "PENDIENTE" -> "ENVIADO"
            "ENVIADO" -> "ENTREGADO"
            else -> null
        }
    }

    fun avanzarEstado(pedido: PedidoResponse) {
        val nuevoEstado = siguienteEstadoDe(pedido.estado) ?: return
        _uiState.value = _uiState.value.copy(actualizandoId = pedido.id)
        viewModelScope.launch {
            val resultado = repository.cambiarEstado(pedido.id, nuevoEstado)
            _uiState.value = resultado.fold(
                onSuccess = { actualizado ->
                    val listaActualizada = _uiState.value.pedidos.map { if (it.id == actualizado.id) actualizado else it }
                    _uiState.value.copy(actualizandoId = null, pedidos = listaActualizada)
                },
                onFailure = { excepcion -> _uiState.value.copy(actualizandoId = null, error = excepcion.message) }
            )
        }
    }

    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null, error = null)
    }
}
