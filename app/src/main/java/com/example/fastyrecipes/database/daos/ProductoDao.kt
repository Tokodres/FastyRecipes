package com.example.fastyrecipes.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fastyrecipes.modelo.Producto

@Dao
interface ProductoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProducto(producto: Producto): Long

    @Query("SELECT * FROM productos")
    suspend fun obtenerTodosLosProductos(): List<Producto>
}