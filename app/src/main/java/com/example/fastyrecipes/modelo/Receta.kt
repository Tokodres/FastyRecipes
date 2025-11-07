package com.example.fastyrecipes.Modelo

data class Receta(
    val id: Long? = null,
    val nombre: String,
    val descripcion: String,
    val tiempoPreparacion: Int,
    val imagenUrl: String? = null,
    val ingredientes: List<Producto>,
    val instrucciones: List<String>,
    val categoria: String,
    val esFavorita: Boolean = false
)