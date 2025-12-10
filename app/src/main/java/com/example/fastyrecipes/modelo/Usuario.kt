package com.example.fastyrecipes.modelo

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val correo: String = "",
    val contraseña: String = "",
    val fechaRegistro: Long = System.currentTimeMillis(),
    val recetasGuardadas: List<String> = emptyList(),
    val historialBusquedas: List<String> = emptyList(),
    val fotoPerfil: String = "",
    val idiomaPreferido: String = "es" // NUEVO CAMPO
) {
    constructor() : this("", "", "", "", 0L, emptyList(), emptyList(), "", "es")
}