package com.example.fastyrecipes.Modelo

data class Usuario(
    val id: Long? = null,
    val nombre: String,
    val correo: String,
    val contraseña: String,
    val recetasGuardadas: List<Receta> = emptyList(),
    val historialBusquedas: List<String> = emptyList()
)