package com.catalogoapp.consultoras.data.repository

import com.catalogoapp.consultoras.data.model.Producto
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val coleccion = FirebaseFirestore.getInstance().collection("productos")

    fun observarCatalogo(): Flow<List<Producto>> = callbackFlow {
        val registro = coleccion.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val productos = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Producto::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(productos)
        }
        awaitClose { registro.remove() }
    }

    suspend fun sembrarProductosDeEjemplo() {
        val ejemplos = listOf(
            Producto(nombre = "Labial Mate Rosa", categoria = "Maquillaje", precio = 29.9, descripcion = "Larga duracion", stock = 40),
            Producto(nombre = "Base Liquida Natural", categoria = "Maquillaje", precio = 45.5, descripcion = "Cobertura media", stock = 25),
            Producto(nombre = "Crema Facial Hidratante", categoria = "Tratamiento facial", precio = 38.0, descripcion = "Con acido hialuronico", stock = 30),
            Producto(nombre = "Serum Antiedad", categoria = "Tratamiento facial", precio = 65.0, descripcion = "Con vitamina C", stock = 15),
            Producto(nombre = "Perfume Floral 50ml", categoria = "Fragancias", precio = 89.9, descripcion = "Notas florales", stock = 20),
            Producto(nombre = "Vela Aromatica Vainilla", categoria = "Velas", precio = 24.9, descripcion = "40 horas de duracion", stock = 35)
        )
        ejemplos.forEach { producto ->
            val doc = coleccion.document()
            doc.set(producto.copy(id = doc.id)).await()
        }
    }

    suspend fun actualizarStockPorDevolucion(productoId: String, apto: Boolean) {
        if (apto) {
            coleccion.document(productoId).update("stock", FieldValue.increment(1)).await()
        }
    }
}