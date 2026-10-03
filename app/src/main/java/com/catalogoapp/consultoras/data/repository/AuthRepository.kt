package com.catalogoapp.consultoras.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun registrar(email: String, password: String): Result<Unit> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.sendEmailVerification()?.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun iniciarSesion(email: String, password: String): Result<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Usuario no encontrado")
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun enviarCorreoRecuperacion(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun cerrarSesion() {
        auth.signOut()
    }
}
