package com.example.fastyrecipes.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastyrecipes.controller.FastyController
import com.example.fastyrecipes.modelo.Receta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecetasViewModel(private val controller: FastyController) : ViewModel() {

    // Flow que observa cambios en tiempo real de la base de datos
    val recetas = controller.obtenerTodasLasRecetas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // CORREGIDO: Usar StateFlow en lugar de mutableStateOf
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Cargar datos iniciales en la base de datos
                controller.cargarDatosIniciales()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error inicializando datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorito(receta: Receta) {
        viewModelScope.launch {
            try {
                controller.marcarComoFavorita(receta.id, !receta.esFavorita)
            } catch (e: Exception) {
                _error.value = "Error actualizando favorito: ${e.message}"
            }
        }
    }

    fun agregarReceta(nombre: String, descripcion: String, tiempo: Int, categoria: String) {
        viewModelScope.launch {
            try {
                val nuevaReceta = Receta(
                    nombre = nombre,
                    descripcion = descripcion,
                    tiempoPreparacion = tiempo,
                    categoria = categoria
                )
                controller.insertarReceta(nuevaReceta)
            } catch (e: Exception) {
                _error.value = "Error agregando receta: ${e.message}"
            }
        }
    }

    fun recargarDatos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                controller.cargarDatosIniciales()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error recargando datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarReceta(receta: Receta) {
        viewModelScope.launch {
            try {
                controller.eliminarReceta(receta)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error eliminando receta: ${e.message}"
            }
        }
    }

    fun eliminarTodasLasRecetas() {
        viewModelScope.launch {
            try {
                // Obtener todas las recetas y eliminarlas una por una
                val todasRecetas = recetas.value
                todasRecetas.forEach { receta ->
                    controller.eliminarReceta(receta)
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error eliminando todas las recetas: ${e.message}"
            }
        }
    }
}