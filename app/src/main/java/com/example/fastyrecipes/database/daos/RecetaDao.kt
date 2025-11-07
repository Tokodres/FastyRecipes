package com.example.fastyrecipes.database.daos

import androidx.room.*
import com.example.fastyrecipes.modelo.Receta
import kotlinx.coroutines.flow.Flow

@Dao
interface RecetaDao {

    // Flow para observar cambios en tiempo real (usar en UI)
    @Query("SELECT * FROM recetas")
    fun obtenerTodasLasRecetas(): Flow<List<Receta>>

    // List normal (usar en suspend functions)
    @Query("SELECT * FROM recetas")
    suspend fun obtenerTodasLasRecetasLista(): List<Receta>

    @Query("SELECT * FROM recetas WHERE id = :id")
    suspend fun obtenerRecetaPorId(id: Long): Receta?

    // Flow para categorías
    @Query("SELECT * FROM recetas WHERE categoria = :categoria")
    fun obtenerRecetasPorCategoria(categoria: String): Flow<List<Receta>>

    // List normal para categorías
    @Query("SELECT * FROM recetas WHERE categoria = :categoria")
    suspend fun obtenerRecetasPorCategoriaLista(categoria: String): List<Receta>

    @Query("SELECT * FROM recetas WHERE nombre LIKE '%' || :query || '%' OR descripcion LIKE '%' || :query || '%'")
    suspend fun buscarRecetas(query: String): List<Receta>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReceta(receta: Receta): Long

    @Update
    suspend fun actualizarReceta(receta: Receta)

    @Query("UPDATE recetas SET es_favorita = :esFavorita WHERE id = :recetaId")
    suspend fun actualizarFavorito(recetaId: Long, esFavorita: Boolean)

    @Delete
    suspend fun eliminarReceta(receta: Receta)

    @Query("DELETE FROM recetas WHERE id = :id")
    suspend fun eliminarRecetaPorId(id: Long)
}