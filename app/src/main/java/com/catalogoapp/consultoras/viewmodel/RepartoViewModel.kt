package com.catalogoapp.consultoras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalogoapp.consultoras.data.model.CrearRepartoRequest
import com.catalogoapp.consultoras.data.model.PedidoResponse
import com.catalogoapp.consultoras.data.model.RepartoResponse
import com.catalogoapp.consultoras.data.model.ResumenRepartoDto
import com.catalogoapp.consultoras.data.repository.PedidoRepository
import com.catalogoapp.consultoras.data.repository.RepartoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RepartoUiState(
    val cargandoPedidos: Boolean = true,
    val pedidosDisponibles: List<PedidoResponse> = emptyList(),
    val pedidosSeleccionados: Set<Int> = emptySet(),
    val zona: String = "",
    val chofer: String = "",
    val vehiculo: String = "",
    val direccionOrigen: String = "",
    val creando: Boolean = false,
    val repartoCreado: RepartoResponse? = null,
    val repartoActual: RepartoResponse? = null,
    val cargandoReparto: Boolean = false,
    val actualizandoParadaId: Int? = null,
    val repartos: List<ResumenRepartoDto> = emptyList(),
    val cargandoRepartos: Boolean = false,
    val error: String? = null
)

class RepartoViewModel @JvmOverloads constructor(
    private val repartoRepository: RepartoRepository = RepartoRepository(),
    private val pedidoRepository: PedidoRepository = PedidoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepartoUiState())
    val uiState: StateFlow<RepartoUiState> = _uiState

    init {
        cargarPedidosDisponibles()
    }

    fun cargarPedidosDisponibles() {
        _uiState.value = _uiState.value.copy(cargandoPedidos = true)
        viewModelScope.launch {
            val resultado = pedidoRepository.listarPedidos()
            _uiState.value = resultado.fold(
                onSuccess = { lista ->
                    val disponibles = lista.filter { it.estado != "ENTREGADO" && it.direccion.isNotBlank() }
                    _uiState.value.copy(cargandoPedidos = false, pedidosDisponibles = disponibles, error = null)
                },
                onFailure = { excepcion -> _uiState.value.copy(cargandoPedidos = false, error = excepcion.message) }
            )
        }
    }

    fun actualizarZona(valor: String) {
        _uiState.value = _uiState.value.copy(zona = valor)
    }

    fun actualizarChofer(valor: String) {
        _uiState.value = _uiState.value.copy(chofer = valor)
    }

    fun actualizarVehiculo(valor: String) {
        _uiState.value = _uiState.value.copy(vehiculo = valor)
    }

    fun actualizarDireccionOrigen(valor: String) {
        _uiState.value = _uiState.value.copy(direccionOrigen = valor)
    }

    fun alternarPedidoSeleccionado(idPedido: Int) {
        val actuales = _uiState.value.pedidosSeleccionados.toMutableSet()
        if (actuales.contains(idPedido)) actuales.remove(idPedido) else actuales.add(idPedido)
        _uiState.value = _uiState.value.copy(pedidosSeleccionados = actuales)
    }

    fun formularioValido(): Boolean {
        val estado = _uiState.value
        return estado.zona.isNotBlank() &&
            estado.chofer.isNotBlank() &&
            estado.vehiculo.isNotBlank() &&
            estado.direccionOrigen.isNotBlank() &&
            estado.pedidosSeleccionados.isNotEmpty()
    }

    fun crearReparto() {
        val estado = _uiState.value
        if (!formularioValido()) {
            _uiState.value = estado.copy(error = "Completa zona, chofer, vehiculo, direccion de origen y selecciona al menos un pedido")
            return
        }
        _uiState.value = estado.copy(creando = true, error = null)
        viewModelScope.launch {
            val datos = CrearRepartoRequest(
                zona = estado.zona,
                chofer = estado.chofer,
                vehiculo = estado.vehiculo,
                direccionOrigen = estado.direccionOrigen,
                pedidoIds = estado.pedidosSeleccionados.toList()
            )
            val resultado = repartoRepository.crearReparto(datos)
            _uiState.value = resultado.fold(
                onSuccess = { reparto -> _uiState.value.copy(creando = false, repartoCreado = reparto) },
                onFailure = { excepcion -> _uiState.value.copy(creando = false, error = excepcion.message ?: "No se pudo crear el reparto") }
            )
        }
    }

    fun cargarRepartos() {
        _uiState.value = _uiState.value.copy(cargandoRepartos = true)
        viewModelScope.launch {
            val resultado = repartoRepository.listarRepartos()
            _uiState.value = resultado.fold(
                onSuccess = { lista -> _uiState.value.copy(cargandoRepartos = false, repartos = lista, error = null) },
                onFailure = { excepcion -> _uiState.value.copy(cargandoRepartos = false, error = excepcion.message) }
            )
        }
    }

    fun cargarReparto(id: Int) {
        _uiState.value = _uiState.value.copy(cargandoReparto = true)
        viewModelScope.launch {
            val resultado = repartoRepository.obtenerReparto(id)
            _uiState.value = resultado.fold(
                onSuccess = { reparto -> _uiState.value.copy(cargandoReparto = false, repartoActual = reparto, error = null) },
                onFailure = { excepcion -> _uiState.value.copy(cargandoReparto = false, error = excepcion.message) }
            )
        }
    }

    fun marcarParadaEntregada(idReparto: Int, idParada: Int) {
        _uiState.value = _uiState.value.copy(actualizandoParadaId = idParada)
        viewModelScope.launch {
            val resultado = repartoRepository.marcarParadaEntregada(idReparto, idParada)
            _uiState.value = resultado.fold(
                onSuccess = {
                    _uiState.value.copy(actualizandoParadaId = null)
                },
                onFailure = { excepcion -> _uiState.value.copy(actualizandoParadaId = null, error = excepcion.message) }
            )
            if (resultado.isSuccess) {
                cargarReparto(idReparto)
            }
        }
    }

    fun actualizarMiUbicacion(idReparto: Int, latitud: Double, longitud: Double) {
        viewModelScope.launch {
            repartoRepository.actualizarUbicacion(idReparto, latitud, longitud)
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
