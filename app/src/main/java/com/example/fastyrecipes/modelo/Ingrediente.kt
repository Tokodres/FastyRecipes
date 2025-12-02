package com.example.fastyrecipes.modelo

import com.google.firebase.firestore.PropertyName

data class Ingrediente(
    val nombre: String = "",
    @PropertyName("cantidad")
    val cantidad: String = "",  // Ej: "200g", "2 cucharadas", "1 taza"
    val id: String = ""
) {
    // Constructor alternativo para Firestore
    constructor() : this( "", "", "")

    // Función para mostrar el ingrediente formateado
    fun mostrar(): String {
        return if (cantidad.isNotEmpty()) {
            "$cantidad  de $nombre"
        } else {
            "$cantidad $nombre"
        }
    }
}