package com.example.fastyrecipes.modelo

data class Usuario(
    val id: String = "",  // ID único de Firebase
    val nombre: String = "",
    val correo: String = "",
    val contraseña: String = "",  // En producción debería estar encriptada
    val recetasGuardadas: List<String> = emptyList(),  // IDs de recetas
    val historialBusquedas: List<String> = emptyList(),
    val fechaRegistro: Long = System.currentTimeMillis(),
    val fotoPerfil: String = ""  // URL de la foto de perfil
) {
    // Constructor vacío para Firestore
    constructor() : this("", "", "", "", emptyList(), emptyList(), 0L, "")
}