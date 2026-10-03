package com.catalogoapp.consultoras.data.repository

import com.catalogoapp.consultoras.data.model.ConsultoraProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProfileRepository {

    private val db = FirebaseFirestore.getInstance()
    private val coleccion = db.collection("consultoras")
    private fun documento(uid: String) = coleccion.document(uid)

    suspend fun guardarPerfil(perfil: ConsultoraProfile): Result<Unit> = try {
        documento(perfil.uid).set(perfil).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun obtenerPerfil(uid: String): Result<ConsultoraProfile?> = try {
        val snapshot = documento(uid).get().await()
        Result.success(snapshot.toObject(ConsultoraProfile::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun observarConsultoras(): Flow<List<ConsultoraProfile>> = callbackFlow {
        val registro = coleccion.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val consultoras = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ConsultoraProfile::class.java)
            } ?: emptyList()
            trySend(consultoras)
        }
        awaitClose { registro.remove() }
    }

    suspend fun actualizarEstado(uid: String, activo: Boolean): Result<Unit> = try {
        documento(uid).update("activo", activo).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}