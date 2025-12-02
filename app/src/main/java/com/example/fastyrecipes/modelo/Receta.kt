package com.example.fastyrecipes.modelo

import com.google.firebase.firestore.PropertyName

data class Receta(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",

    @PropertyName("tiempo_preparacion")
    val tiempoPreparacion: Int = 0,

    @PropertyName("imagen_url")
    val imagenUrl: String = "",

    val categoria: String = "",

    @PropertyName("es_favorita")
    val esFavorita: Boolean = false,

    // CAMBIADO: Ahora es List<Ingrediente> en lugar de List<String>
    val ingredientes: List<Ingrediente> = emptyList(),
    val pasos: List<String> = emptyList()
) {
    // Constructor alternativo CORREGIDO
    constructor() : this("", "", "", 0, "", "", false, emptyList(), emptyList())

    // Función para obtener el número total de ingredientes
    fun getCantidadIngredientes(): Int {
        return ingredientes.size
    }
}