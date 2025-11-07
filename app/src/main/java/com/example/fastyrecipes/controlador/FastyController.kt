package com.example.fastyrecipes.controller

import android.content.Context
import com.example.fastyrecipes.database.FastyRecipesDatabase
import com.example.fastyrecipes.modelo.Receta
import kotlinx.coroutines.flow.Flow

class FastyController(context: Context) {

    private val database = FastyRecipesDatabase.getDatabase(context)
    private val recetaDao = database.recetaDao()

    // Flow para observar cambios en tiempo real
    fun obtenerTodasLasRecetas(): Flow<List<Receta>> {
        return recetaDao.obtenerTodasLasRecetas()
    }

    fun obtenerRecetasPorCategoria(categoria: String): Flow<List<Receta>> {
        return recetaDao.obtenerRecetasPorCategoria(categoria)
    }

    suspend fun buscarRecetas(query: String): List<Receta> {
        return recetaDao.buscarRecetas(query)
    }

    suspend fun insertarReceta(receta: Receta): Long {
        return recetaDao.insertarReceta(receta)
    }

    suspend fun marcarComoFavorita(recetaId: Long, esFavorita: Boolean) {
        recetaDao.actualizarFavorito(recetaId, esFavorita)
    }

    suspend fun obtenerRecetaPorId(id: Long): Receta? {
        return recetaDao.obtenerRecetaPorId(id)
    }

    // ↓↓↓ FUNCIONES DE ELIMINACIÓN CORREGIDAS - FUERA DE cargarDatosIniciales ↓↓↓
    suspend fun eliminarReceta(receta: Receta) {
        recetaDao.eliminarReceta(receta)
    }

    suspend fun eliminarRecetaPorId(id: Long) {
        val receta = recetaDao.obtenerRecetaPorId(id)
        receta?.let { recetaDao.eliminarReceta(it) }
    }
    // ↑↑↑ FUNCIONES DE ELIMINACIÓN CORREGIDAS ↑↑↑

    // Función para cargar datos iniciales
    suspend fun cargarDatosIniciales() {
        // Verificar si ya hay datos
        val recetasExistentes = recetaDao.obtenerTodasLasRecetasLista()

        if (recetasExistentes.isEmpty()) {
            // Insertar datos de ejemplo solo si la BD está vacía
            val recetasEjemplo = listOf(
                Receta(
                    nombre = "Pollo al Horno",
                    descripcion = "Delicioso pollo horneado con especias",
                    tiempoPreparacion = 40,
                    categoria = "Cena"
                ),
                Receta(
                    nombre = "Ensalada César",
                    descripcion = "Ensalada fresca con pollo y aderezo césar",
                    tiempoPreparacion = 15,
                    categoria = "Almuerzo"
                ),
                Receta(
                    nombre = "Brownies de Chocolate",
                    descripcion = "Postre de chocolate irresistible",
                    tiempoPreparacion = 30,
                    categoria = "Postre"
                ),
                Receta(
                    nombre = "Sopa de Tomate",
                    descripcion = "Sopa cremosa de tomate natural",
                    tiempoPreparacion = 25,
                    categoria = "Entrada"
                ),
                Receta(
                    nombre = "Pasta Carbonara",
                    descripcion = "Clásica pasta italiana con salsa cremosa",
                    tiempoPreparacion = 20,
                    categoria = "Almuerzo",
                    esFavorita = true
                )
            )

            recetasEjemplo.forEach { receta ->
                recetaDao.insertarReceta(receta)
            }
        }
    }
}