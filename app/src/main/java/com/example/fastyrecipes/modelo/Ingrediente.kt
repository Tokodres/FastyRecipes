package com.example.fastyrecipes.modelo

import com.google.firebase.firestore.PropertyName

data class Ingrediente(
    val nombre: String = "",
    @PropertyName("cantidad")
    val cantidad: String = "",  // Ej: "200g", "2 cucharadas", "1 taza"
    @PropertyName("unidad")
    val unidad: String = "",    // Ej: "gramos", "cucharadas", "tazas"
    val id: String = ""
) {
    // Constructor alternativo para Firestore
    constructor() : this("", "", "", "")

    // Función para mostrar el ingrediente formateado
    fun mostrar(): String {
        return if (unidad.isNotEmpty()) {
            "$cantidad $unidad de $nombre"
        } else {
            "$cantidad $nombre"
        }
    }
}