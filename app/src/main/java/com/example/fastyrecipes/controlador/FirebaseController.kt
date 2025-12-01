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

    // Obtener todas las recetas en tiempo real con Flow - CON DEBUG MEJORADO
    fun obtenerTodasLasRecetas(): Flow<List<Receta>> = callbackFlow {
        val listener = recetasCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("❌ ERROR en snapshot listener: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }

            val recetas = snapshot?.documents?.mapNotNull { document ->
                try {
                    val receta = document.toObject(Receta::class.java)?.copy(id = document.id)

                    // DEBUG MEJORADO: Verificar cada receta cargada
                    println("🔥 DEBUG FirebaseController - Receta cargada:")
                    println("   - ID: ${receta?.id}")
                    println("   - Nombre: ${receta?.nombre}")
                    println("   - URL: '${receta?.imagenUrl}'")
                    println("   - ¿URL null?: ${receta?.imagenUrl == null}")
                    println("   - ¿URL empty?: ${receta?.imagenUrl?.isEmpty() ?: true}")

                    receta
                } catch (e: Exception) {
                    println("❌ ERROR mapeando receta: ${e.message}")
                    null
                }
            } ?: emptyList()

            // DEBUG: Resumen de carga
            println("📊 RESUMEN FirebaseController:")
            println("   - Total recetas cargadas: ${recetas.size}")
            println("   - Recetas con URL: ${recetas.count { !it.imagenUrl.isNullOrEmpty() }}")
            println("   - Recetas sin URL: ${recetas.count { it.imagenUrl.isNullOrEmpty() }}")

            trySend(recetas)
        }

        awaitClose {
            println("🛑 FirebaseController - Cerrando listener")
            listener.remove()
        }
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

    // Insertar receta - CON DEBUG MEJORADO
    suspend fun insertarReceta(receta: Receta): String {
        try {
            val document = recetasCollection.document()
            val recetaData = hashMapOf(
                "nombre" to receta.nombre,
                "descripcion" to receta.descripcion,
                "tiempo_preparacion" to receta.tiempoPreparacion,
                "categoria" to receta.categoria,
                "es_favorita" to receta.esFavorita,
                "imagen_url" to receta.imagenUrl
            )

            // DEBUG: Verificar qué datos se van a guardar
            println("💾 DEBUG FirebaseController - insertarReceta:")
            println("   - Nombre: ${receta.nombre}")
            println("   - URL a guardar: '${receta.imagenUrl}'")
            println("   - ¿URL null?: ${receta.imagenUrl == null}")
            println("   - Todos los datos: $recetaData")

            document.set(recetaData).await()

            println("✅ FirebaseController - Receta guardada exitosamente")
            println("   - ID generado: ${document.id}")

            return document.id
        } catch (e: Exception) {
            println("❌ ERROR FirebaseController insertando receta: ${e.message}")
            throw e
        }
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

    // Cargar datos iniciales - CON DEBUG MEJORADO
    suspend fun cargarDatosIniciales() {
        try {
            val snapshot = recetasCollection.get().await()
            println("📋 FirebaseController - Verificando datos iniciales:")
            println("   - Recetas existentes: ${snapshot.size()}")

            if (snapshot.isEmpty) {
                println("🔄 FirebaseController - Cargando datos de ejemplo...")

                val recetasEjemplo = listOf(
                    hashMapOf(
                        "nombre" to "Pollo al Horno",
                        "descripcion" to "Delicioso pollo horneado con especias",
                        "tiempo_preparacion" to 40,
                        "categoria" to "Cena",
                        "es_favorita" to false,
                        "imagen_url" to "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=400"
                    ),
                    hashMapOf(
                        "nombre" to "Ensalada César",
                        "descripcion" to "Ensalada fresca con pollo y aderezo césar",
                        "tiempo_preparacion" to 15,
                        "categoria" to "Almuerzo",
                        "es_favorita" to false,
                        "imagen_url" to "https://images.unsplash.com/photo-1546793665-c74683f339c1?w=400"
                    ),
                    hashMapOf(
                        "nombre" to "Brownies de Chocolate",
                        "descripcion" to "Postre de chocolate irresistible",
                        "tiempo_preparacion" to 30,
                        "categoria" to "Postre",
                        "es_favorita" to false,
                        "imagen_url" to "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=400"
                    ),
                )

                recetasEjemplo.forEachIndexed { index, recetaData ->
                    recetasCollection.add(recetaData).await()
                    println("   - Receta ejemplo ${index + 1} agregada")
                }

                println("✅ FirebaseController - Datos iniciales cargados exitosamente")
            } else {
                println("✅ FirebaseController - Ya existen recetas, no se cargan datos iniciales")
            }
        } catch (e: Exception) {
            println("❌ ERROR FirebaseController cargando datos iniciales: ${e.message}")
            throw e
        }
    }

    // Función adicional para agregar receta simple (alternativa)
    suspend fun agregarRecetaSimple(receta: Receta) {
        insertarReceta(receta)
    }
}