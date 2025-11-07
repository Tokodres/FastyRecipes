package com.example.fastyrecipes.modelo

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "receta_ingredientes",
    foreignKeys = [
        ForeignKey(
            entity = Receta::class,
            parentColumns = ["id"],
            childColumns = ["recetaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecetaIngrediente(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recetaId: Long,
    val productoId: Long,
    val cantidad: Int
)