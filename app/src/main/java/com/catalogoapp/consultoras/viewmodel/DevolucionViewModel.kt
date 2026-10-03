package com.catalogoapp.consultoras.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalogoapp.consultoras.data.model.Producto
import com.catalogoapp.consultoras.data.repository.ProductRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class DevolucionUiState(
    val productos: List<Producto> = emptyList(),
    val productoSeleccionado: Producto? = null,
    val foto: Bitmap? = null,
    val clasificando: Boolean = false,
    val resultadoApto: Boolean? = null,
    val etiquetaDetectada: String? = null,
    val confianza: Float? = null,
    val actualizando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)

class DevolucionViewModel @JvmOverloads constructor(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevolucionUiState())
    val uiState: StateFlow<DevolucionUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.observarCatalogo()
                .catch { }
                .collect { productos ->
                    _uiState.value = _uiState.value.copy(productos = productos)
                }
        }
    }

    fun seleccionarProducto(producto: Producto) {
        _uiState.value = _uiState.value.copy(
            productoSeleccionado = producto,
            foto = null,
            resultadoApto = null,
            etiquetaDetectada = null,
            confianza = null,
            mensaje = null,
            error = null
        )
    }

    fun registrarFoto(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(
            foto = bitmap,
            resultadoApto = null,
            etiquetaDetectada = null,
            confianza = null,
            mensaje = null,
            error = null
        )
    }

    fun clasificarProducto() {
        val bitmap = _uiState.value.foto ?: return
        _uiState.value = _uiState.value.copy(clasificando = true, error = null)
        viewModelScope.launch {
            try {
                val etiquetador = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                val imagenEntrada = InputImage.fromBitmap(bitmap, 0)
                val etiquetas = etiquetador.process(imagenEntrada).await()
                val mejor = etiquetas.maxByOrNull { it.confidence }
                val confianza = mejor?.confidence ?: 0f
                val apto = confianza >= 0.65f
                _uiState.value = _uiState.value.copy(
                    clasificando = false,
                    resultadoApto = apto,
                    etiquetaDetectada = mejor?.text ?: "Sin deteccion clara",
                    confianza = confianza
                )
            } catch (excepcion: Exception) {
                _uiState.value = _uiState.value.copy(clasificando = false, error = excepcion.message)
            }
        }
    }

    fun confirmarActualizacionInventario() {
        val producto = _uiState.value.productoSeleccionado ?: return
        val apto = _uiState.value.resultadoApto ?: return
        _uiState.value = _uiState.value.copy(actualizando = true, error = null)
        viewModelScope.launch {
            try {
                repository.actualizarStockPorDevolucion(producto.id, apto)
                val mensaje = if (apto) {
                    "Producto apto: se repuso 1 unidad al inventario"
                } else {
                    "Producto no apto: descartado, inventario sin cambios"
                }
                _uiState.value = _uiState.value.copy(actualizando = false, mensaje = mensaje)
            } catch (excepcion: Exception) {
                _uiState.value = _uiState.value.copy(actualizando = false, error = excepcion.message)
            }
        }
    }

    fun reiniciar() {
        _uiState.value = _uiState.value.copy(
            productoSeleccionado = null,
            foto = null,
            resultadoApto = null,
            etiquetaDetectada = null,
            confianza = null,
            mensaje = null,
            error = null
        )
    }
}