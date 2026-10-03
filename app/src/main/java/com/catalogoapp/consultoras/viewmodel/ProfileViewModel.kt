package com.catalogoapp.consultoras.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.catalogoapp.consultoras.data.model.ConsultoraProfile
import com.catalogoapp.consultoras.data.repository.PedidoRepository
import com.catalogoapp.consultoras.data.repository.ProfileRepository
import com.catalogoapp.consultoras.worker.ProfileSyncWorker
import com.catalogoapp.consultoras.worker.RecordatorioPedidoWorker
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

sealed class ProfileUiState {
    data object Idle : ProfileUiState()
    data object Guardando : ProfileUiState()
    data class Guardado(val mensaje: String) : ProfileUiState()
    data class Error(val mensaje: String) : ProfileUiState()
}

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository(),
    private val pedidoRepository: PedidoRepository = PedidoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _perfil = MutableStateFlow<ConsultoraProfile?>(null)
    val perfil: StateFlow<ConsultoraProfile?> = _perfil

    fun cargarPerfil(uid: String) {
        viewModelScope.launch {
            val resultado = repository.obtenerPerfil(uid)
            resultado.onSuccess { perfilEncontrado ->
                _perfil.value = perfilEncontrado
            }
        }
    }

    fun guardarPerfil(
        context: Context,
        uid: String,
        email: String,
        nombre: String,
        preferencias: String,
        tienePhoto: Boolean
    ) {
        _uiState.value = ProfileUiState.Guardando
        viewModelScope.launch {
            val perfil = ConsultoraProfile(
                uid = uid,
                nombre = nombre,
                email = email,
                preferencias = preferencias,
                tienePhotoLocal = tienePhoto
            )
            val resultado = repository.guardarPerfil(perfil)

            _uiState.value = resultado.fold(
                onSuccess = {
                    _perfil.value = perfil
                    encolarSincronizacion(context, uid, nombre, email, preferencias)
                    ProfileUiState.Guardado("Perfil guardado y sincronizandose en segundo plano")
                },
                onFailure = { ProfileUiState.Error(it.message ?: "No se pudo guardar el perfil") }
            )
        }
    }

    private fun encolarSincronizacion(
        context: Context,
        uid: String,
        nombre: String,
        email: String,
        preferencias: String
    ) {
        val datos = Data.Builder()
            .putString(ProfileSyncWorker.KEY_UID, uid)
            .putString(ProfileSyncWorker.KEY_NOMBRE, nombre)
            .putString(ProfileSyncWorker.KEY_EMAIL, email)
            .putString(ProfileSyncWorker.KEY_PREFERENCIAS, preferencias)
            .build()

        val solicitud = OneTimeWorkRequestBuilder<ProfileSyncWorker>()
            .setInputData(datos)
            .build()

        WorkManager.getInstance(context).enqueue(solicitud)
    }

    fun registrarTokenFcm() {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                pedidoRepository.registrarDispositivo(token)
            } catch (e: Exception) {
            }
        }
    }

    fun programarRecordatorioPedidos(context: Context) {
        val solicitud = PeriodicWorkRequestBuilder<RecordatorioPedidoWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            RecordatorioPedidoWorker.NOMBRE_TRABAJO,
            ExistingPeriodicWorkPolicy.KEEP,
            solicitud
        )
    }
}
