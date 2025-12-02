package com.example.fastyrecipes.modelo

import com.google.firebase.firestore.PropertyName

data class Receta(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    @PropertyName("tiempo_preparacion")
    val tiempoPreparacion: Int = 0,
    @PropertyName("imagen_url")
    val imagenUrl: String = "",  // CAMBIADO: Ahora es String (no nullable)
    val categoria: String = "",
    @PropertyName("es_favorita")
    val esFavorita: Boolean = false
) {
    // Constructor alternativo CORREGIDO
    constructor() : this("", "", "", 0, "", "", false)
}