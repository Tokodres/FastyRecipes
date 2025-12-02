package com.example.fastyrecipes.controller

import com.example.fastyrecipes.modelo.Ingrediente
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
                    // DEBUG EXTENDIDO: Ver qué campos tiene el documento
                    println("📄 DEBUG Firestore - Documento ID: ${document.id}")
                    println("   - Campos disponibles: ${document.data?.keys}")
                    println("   - imagen_url raw: '${document.getString("imagen_url")}'")

                    // Mapear ingredientes
                    val ingredientesList = (document.get("ingredientes") as? List<*>)?.mapNotNull { item ->
                        if (item is Map<*, *>) {
                            Ingrediente(
                                nombre = item["nombre"] as? String ?: "",
                                cantidad = item["cantidad"] as? String ?: "",
                                unidad = item["unidad"] as? String ?: ""
                            )
                        } else {
                            null
                        }
                    } ?: emptyList()

                    // Mapear pasos
                    val pasosList = (document.get("pasos") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                    // Crear y retornar la receta
                    Receta(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "",
                        descripcion = document.getString("descripcion") ?: "",
                        tiempoPreparacion = document.getLong("tiempo_preparacion")?.toInt() ?: 0,
                        categoria = document.getString("categoria") ?: "",
                        esFavorita = document.getBoolean("es_favorita") ?: false,
                        imagenUrl = document.getString("imagen_url") ?: "",
                        ingredientes = ingredientesList,
                        pasos = pasosList
                    )
                } catch (e: Exception) {
                    println("❌ ERROR mapeando receta ${document.id}: ${e.message}")
                    null
                }
            } ?: emptyList()

            // DEBUG: Resumen final
            println("📊 RESUMEN Firestore - Recetas cargadas: ${recetas.size}")
            recetas.forEach { receta ->
                println("   - ${receta.nombre}: imagenUrl = '${receta.imagenUrl}'")
            }

            trySend(recetas)
        }

        awaitClose {
            println("🛑 Firestore - Cerrando listener")
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
                "imagen_url" to receta.imagenUrl,
                "ingredientes" to receta.ingredientes.map { ingrediente ->
                    hashMapOf(
                        "nombre" to ingrediente.nombre,
                        "cantidad" to ingrediente.cantidad,
                        "unidad" to ingrediente.unidad
                    )
                },
                "pasos" to receta.pasos
            )

            document.set(recetaData).await()
            return document.id
        } catch (e: Exception) {
            println("❌ ERROR insertando receta: ${e.message}")
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

                // LISTA DE IMÁGENES DE COMIDA DE UNSPLASH
                val recetasConImagenes = listOf(
                    hashMapOf(
                        "nombre" to "Pollo al Horno",
                        "descripcion" to "Delicioso pollo horneado con especias, perfecto para una cena familiar",
                        "tiempo_preparacion" to 45,
                        "categoria" to "Cena",
                        "es_favorita" to true,
                        "imagen_url" to "https://images.unsplash.com/photo-1606636660488-16a8646f012c?ixlib=rb-4.0.3&w=400&h=300&fit=crop"
                    ),
                    hashMapOf(
                        "nombre" to "Ensalada César",
                        "descripcion" to "Ensalada fresca con pollo a la parrilla, crutones y aderezo césar casero",
                        "tiempo_preparacion" to 20,
                        "categoria" to "Almuerzo",
                        "es_favorita" to false,
                        "imagen_url" to "https://images.unsplash.com/photo-1546793665-c74683f339c1?ixlib=rb-4.0.3&w=400&h=300&fit=crop"
                    ),
                    hashMapOf(
                        "nombre" to "Pasta Carbonara",
                        "descripcion" to "Clásica pasta italiana con huevo, queso pecorino, panceta y pimienta negra",
                        "tiempo_preparacion" to 25,
                        "categoria" to "Cena",
                        "es_favorita" to true,
                        "imagen_url" to "https://images.unsplash.com/photo-1605478371315-e4c2bf81296a?ixlib=rb-4.0.3&w=400&h=300&fit=crop"
                    ),
                    hashMapOf(
                        "nombre" to "Brownies de Chocolate",
                        "descripcion" to "Postre de chocolate intenso con nueces, perfecto para acompañar con helado",
                        "tiempo_preparacion" to 35,
                        "categoria" to "Postre",
                        "es_favorita" to true,
                        "imagen_url" to "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?ixlib=rb-4.0.3&w=400&h=300&fit=crop"
                    ),
                    hashMapOf(
                        "nombre" to "Hamburguesa Casera",
                        "descripcion" to "Hamburguesa de carne premium con queso, lechuga, tomate y salsa especial",
                        "tiempo_preparacion" to 30,
                        "categoria" to "Almuerzo",
                        "es_favorita" to false,
                        "imagen_url" to "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?ixlib=rb-4.0.3&w=400&h=300&fit=crop"
                    ),
                    hashMapOf(
                        "nombre" to "Sopa de Tomate",
                        "descripcion" to "Sopa cremosa de tomate natural con albahaca fresca y crutones",
                        "tiempo_preparacion" to 40,
                        "categoria" to "Entrada",
                        "es_favorita" to false,
                        "imagen_url" to "https://images.unsplash.com/photo-1547592166-23ac45744acd?ixlib=rb-4.0.3&w=400&h=300&fit=crop"
                    ),
                    hashMapOf(
                        "nombre" to "Tacos al Pastor",
                        "descripcion" to "Auténticos tacos mexicanos con carne adobada, piña y cilantro",
                        "tiempo_preparacion" to 50,
                        "categoria" to "Cena",
                        "es_favorita" to true,
                        "imagen_url" to "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?ixlib=rb-4.0.3&w=400&h=300&fit=crop"
                    ),
                    hashMapOf(
                        "nombre" to "Pizza Margarita",
                        "descripcion" to "Pizza clásica italiana con salsa de tomate, mozzarella fresca y albahaca",
                        "tiempo_preparacion" to 60,
                        "categoria" to "Cena",
                        "es_favorita" to false,
                        "imagen_url" to "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?ixlib=rb-4.0.3&w=400&h=300&fit=crop"
                    ),
                    hashMapOf(
                        "nombre" to "Salmón a la Parrilla",
                        "descripcion" to "Filete de salmón fresco con limón y eneldo, acompañado de vegetales",
                        "tiempo_preparacion" to 25,
                        "categoria" to "Cena",
                        "es_favorita" to true,
                        "imagen_url" to "https://images.unsplash.com/photo-1467003909585-2f8a72700288?ixlib=rb-4.0.3&w=400&h=300&fit=crop"
                    ),
                    hashMapOf(
                        "nombre" to "Tarta de Manzana",
                        "descripcion" to "Deliciosa tarta de manzana casera con canela y vainilla",
                        "tiempo_preparacion" to 70,
                        "categoria" to "Postre",
                        "es_favorita" to false,
                        "imagen_url" to "https://images.unsplash.com/photo-1565958011703-44f9829ba187?ixlib=rb-4.0.3&w=400&h=300&fit=crop"
                    )
                )

                recetasConImagenes.forEachIndexed { index, recetaData ->
                    recetasCollection.add(recetaData).await()
                    println("   - Receta ejemplo ${index + 1} agregada: ${recetaData["nombre"]}")
                    println("   - URL de imagen: ${recetaData["imagen_url"]}")
                }

                println("✅ FirebaseController - Datos iniciales cargados exitosamente")
                println("   - Total recetas: ${recetasConImagenes.size}")
            } else {
                // Verificar que todas las recetas existentes tengan imagen
                snapshot.documents.forEach { doc ->
                    val imagenUrl = doc.getString("imagen_url")
                    if (imagenUrl.isNullOrEmpty()) {
                        println("⚠️  Receta sin imagen encontrada: ${doc.getString("nombre")}")
                    }
                }
                println("✅ FirebaseController - Ya existen ${snapshot.size()} recetas")
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