package com.catalogoapp.consultoras.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalogoapp.consultoras.data.model.ConsultoraProfile
import com.catalogoapp.consultoras.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class AdminConsultorasUiState(
    val cargando: Boolean = true,
    val consultoras: List<ConsultoraProfile> = emptyList(),
    val actualizando: Boolean = false,
    val error: String? = null
)

class AdminConsultorasViewModel(
    private val repository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminConsultorasUiState())
    val uiState: StateFlow<AdminConsultorasUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.observarConsultoras()
                .catch { excepcion ->
                    _uiState.value = _uiState.value.copy(cargando = false, error = excepcion.message)
                }
                .collect { consultoras ->
                    _uiState.value = _uiState.value.copy(cargando = false, consultoras = consultoras)
                }
        }
    }

    fun cambiarEstado(uid: String, activo: Boolean) {
        _uiState.value = _uiState.value.copy(actualizando = true)
        viewModelScope.launch {
            repository.actualizarEstado(uid, activo)
            _uiState.value = _uiState.value.copy(actualizando = false)
        }
    }
}

