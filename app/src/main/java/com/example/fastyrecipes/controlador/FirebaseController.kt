package com.example.fastyrecipes.controller

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.fastyrecipes.modelo.Receta
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseController {

    private val db: FirebaseFirestore = Firebase.firestore
    private val recetasCollection = db.collection("recetas")

    // Obtener todas las recetas en tiempo real con Flow
    fun obtenerTodasLasRecetas(): Flow<List<Receta>> = callbackFlow {
        val listener = recetasCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val recetas = snapshot?.documents?.mapNotNull { document ->
                try {
                    document.toObject(Receta::class.java)?.copy(id = document.id)
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()

            trySend(recetas)
        }

        awaitClose { listener.remove() }
    }

    // Obtener recetas por categoría en tiempo real
    fun obtenerRecetasPorCategoria(categoria: String): Flow<List<Receta>> = callbackFlow {
        val listener = recetasCollection
            .whereEqualTo("categoria", categoria)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val recetas = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(Receta::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(recetas)
            }

        awaitClose { listener.remove() }
    }

    // Buscar recetas
    suspend fun buscarRecetas(query: String): List<Receta> {
        val snapshot = recetasCollection.get().await()
        return snapshot.documents.mapNotNull { document ->
            try {
                document.toObject(Receta::class.java)?.copy(id = document.id)
            } catch (e: Exception) {
                null
            }
        }.filter { receta ->
            receta.nombre.contains(query, ignoreCase = true) ||
                    receta.descripcion.contains(query, ignoreCase = true)
        }
    }

    // Insertar receta
    suspend fun insertarReceta(receta: Receta): String {
        val document = recetasCollection.document()
        val recetaData = hashMapOf(
            "nombre" to receta.nombre,
            "descripcion" to receta.descripcion,
            "tiempo_preparacion" to receta.tiempoPreparacion,
            "categoria" to receta.categoria,
            "es_favorita" to receta.esFavorita,
            "imagen_url" to receta.imagenUrl
        )

        document.set(recetaData).await()
        return document.id
    }

    // Marcar como favorita
    suspend fun marcarComoFavorita(recetaId: String, esFavorita: Boolean) {
        recetasCollection.document(recetaId)
            .update("es_favorita", esFavorita)
            .await()
    }

    // Obtener receta por ID
    suspend fun obtenerRecetaPorId(id: String): Receta? {
        val document = recetasCollection.document(id).get().await()
        return if (document.exists()) {
            document.toObject(Receta::class.java)?.copy(id = document.id)
        } else {
            null
        }
    }

    // Eliminar receta
    suspend fun eliminarReceta(recetaId: String) {
        recetasCollection.document(recetaId).delete().await()
    }

    // Cargar datos iniciales
    suspend fun cargarDatosIniciales() {
        val snapshot = recetasCollection.get().await()
        if (snapshot.isEmpty) {
            val recetasEjemplo = listOf(
                hashMapOf(
                    "nombre" to "Pollo al Horno",
                    "descripcion" to "Delicioso pollo horneado con especias",
                    "tiempo_preparacion" to 40,
                    "categoria" to "Cena",
                    "es_favorita" to false
                ),
                hashMapOf(
                    "nombre" to "Ensalada César",
                    "descripcion" to "Ensalada fresca con pollo y aderezo césar",
                    "tiempo_preparacion" to 15,
                    "categoria" to "Almuerzo",
                    "es_favorita" to false
                ),
                hashMapOf(
                    "nombre" to "Brownies de Chocolate",
                    "descripcion" to "Postre de chocolate irresistible",
                    "tiempo_preparacion" to 30,
                    "categoria" to "Postre",
                    "es_favorita" to false
                ),
                hashMapOf(
                    "nombre" to "Sopa de Tomate",
                    "descripcion" to "Sopa cremosa de tomate natural",
                    "tiempo_preparacion" to 25,
                    "categoria" to "Entrada",
                    "es_favorita" to false
                ),
                hashMapOf(
                    "nombre" to "Pasta Carbonara",
                    "descripcion" to "Clásica pasta italiana con salsa cremosa",
                    "tiempo_preparacion" to 20,
                    "categoria" to "Almuerzo",
                    "es_favorita" to true
                )
            )

            recetasEjemplo.forEach { recetaData ->
                recetasCollection.add(recetaData).await()
            }
        }
    }
}