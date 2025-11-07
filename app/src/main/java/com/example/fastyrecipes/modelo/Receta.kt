package com.example.fastyrecipes.modelo

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "recetas")
data class Receta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val descripcion: String,

    @ColumnInfo(name = "tiempo_preparacion")
    val tiempoPreparacion: Int,

    @ColumnInfo(name = "imagen_url")
    val imagenUrl: String? = null,

    val categoria: String,

    @ColumnInfo(name = "es_favorita")
    val esFavorita: Boolean = false
)