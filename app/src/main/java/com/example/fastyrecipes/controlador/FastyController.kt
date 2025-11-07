package com.example.fastyrecipes.controller

import com.example.fastyrecipes.Modelo.Usuario
import com.example.fastyrecipes.Modelo.Receta
import com.example.fastyrecipes.Modelo.Producto

class FastyController {

    // Datos en memoria (luego los conectarás con Room)
    private val usuarios = mutableListOf<Usuario>()
    private val recetas = mutableListOf<Receta>()
    private var usuarioActual: Usuario? = null

    // --- Gestión de Usuarios ---
    fun registrarUsuario(nombre: String, correo: String, contraseña: String): Boolean {
        val nuevoUsuario = Usuario(
            id = (usuarios.size + 1).toLong(),
            nombre = nombre,
            correo = correo,
            contraseña = contraseña
        )
        return usuarios.add(nuevoUsuario)
    }

    fun login(correo: String, contraseña: String): Boolean {
        usuarioActual = usuarios.find { it.correo == correo && it.contraseña == contraseña }
        return usuarioActual != null
    }

    fun getUsuarioActual(): Usuario? {
        return usuarioActual
    }

    // --- Gestión de Recetas ---
    fun obtenerTodasLasRecetas(): List<Receta> {
        return recetas
    }

    fun buscarRecetas(query: String): List<Receta> {
        // Agregar al historial si hay usuario logueado
        usuarioActual?.historialBusquedas?.toMutableList()?.add(query)

        return recetas.filter {
            it.nombre.contains(query, ignoreCase = true) ||
                    it.descripcion.contains(query, ignoreCase = true) ||
                    it.categoria.contains(query, ignoreCase = true)
        }
    }

    fun obtenerRecetasPorCategoria(categoria: String): List<Receta> {
        return recetas.filter { it.categoria.equals(categoria, ignoreCase = true) }
    }

    // --- Gestión de Favoritos ---
    fun agregarRecetaFavorita(receta: Receta): Boolean {
        usuarioActual?.let { usuario ->
            val nuevasRecetas = usuario.recetasGuardadas.toMutableList()
            nuevasRecetas.add(receta)
            // Actualizar usuario (en una BD real aquí harías update)
            return true
        }
        return false
    }

    fun eliminarRecetaFavorita(receta: Receta): Boolean {
        usuarioActual?.let { usuario ->
            val nuevasRecetas = usuario.recetasGuardadas.toMutableList()
            return nuevasRecetas.remove(receta)
        }
        return false
    }

    fun obtenerRecetasFavoritas(): List<Receta> {
        return usuarioActual?.recetasGuardadas ?: emptyList()
    }

    // --- Datos de Ejemplo (para pruebas) ---
    fun cargarDatosEjemplo() {
        // Productos de ejemplo
        val pollo = Producto(nombre = "Pollo", cantidad = 1)
        val arroz = Producto(nombre = "Arroz", cantidad = 2)
        val tomate = Producto(nombre = "Tomate", cantidad = 3)

        // Recetas de ejemplo
        recetas.add(
            Receta(
                id = 1,
                nombre = "Pollo al Horno",
                descripcion = "Delicioso pollo horneado con especias",
                tiempoPreparacion = 40,
                ingredientes = listOf(pollo),
                instrucciones = listOf("Precalentar horno", "Sazonar pollo", "Hornear por 40 min"),
                categoria = "Cena"
            )
        )

        recetas.add(
            Receta(
                id = 2,
                nombre = "Arroz con Pollo",
                descripcion = "Clásico arroz con pollo estilo colombiano",
                tiempoPreparacion = 30,
                ingredientes = listOf(pollo, arroz, tomate),
                instrucciones = listOf("Sofreír pollo", "Agregar arroz", "Cocinar por 30 min"),
                categoria = "Almuerzo"
            )
        )
    }
}