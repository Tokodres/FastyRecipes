package com.example.fastyrecipes.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastyrecipes.controller.FirebaseController
import com.example.fastyrecipes.modelo.Receta
import com.example.fastyrecipes.modelo.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.example.fastyrecipes.modelo.Ingrediente

class RecetasViewModel(private val firebaseController: FirebaseController) : ViewModel() {

    // Flow que observa cambios en tiempo real de Firebase Firestore
    val recetas = firebaseController.obtenerTodasLasRecetas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Estados de carga y error
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Estados para búsqueda y filtros
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Recetas filtradas por búsqueda y categoría
    val recetasFiltradas: StateFlow<List<Receta>> = combine(
        recetas,
        _searchText,
        _selectedCategory
    ) { recetas, searchText, selectedCategory ->
        recetas.filter { receta ->
            val matchesSearch = searchText.isEmpty() ||
                    receta.nombre.contains(searchText, ignoreCase = true) ||
                    receta.descripcion.contains(searchText, ignoreCase = true)

            val matchesCategory = selectedCategory == null ||
                    receta.categoria.equals(selectedCategory, ignoreCase = true)

            matchesSearch && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Categorías únicas
    val categoriasUnicas: StateFlow<List<String>> = recetas.map { recetas ->
        recetas.map { it.categoria }.distinct()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Recetas favoritas
    val recetasFavoritas: StateFlow<List<Receta>> = recetas.map { recetas ->
        recetas.filter { it.esFavorita }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Estados para múltiples perfiles (usando tu clase Usuario)
    private val _perfilesUsuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val perfilesUsuarios: StateFlow<List<Usuario>> = _perfilesUsuarios.asStateFlow()

    private val _perfilActivo = MutableStateFlow<Usuario?>(null)
    val perfilActivo: StateFlow<Usuario?> = _perfilActivo.asStateFlow()

    init {
        cargarDatosIniciales()
        cargarUsuariosEjemplo()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Cargar datos iniciales en Firebase
                firebaseController.cargarDatosIniciales()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error inicializando datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== FUNCIONES DE BÚSQUEDA Y FILTROS ==========

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }

    fun buscarRecetas(query: String) {
        _searchText.value = query
    }

    fun limpiarBusqueda() {
        _searchText.value = ""
        _selectedCategory.value = null
    }

    // ========== FUNCIONES DE FAVORITOS ==========

    fun obtenerFavoritas(): StateFlow<List<Receta>> = recetasFavoritas

    fun limpiarTodosLosFavoritos() {
        viewModelScope.launch {
            try {
                val todasRecetas = recetas.value
                todasRecetas.forEach { receta ->
                    if (receta.esFavorita) {
                        firebaseController.marcarComoFavorita(receta.id, false)
                    }
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error limpiando favoritos: ${e.message}"
            }
        }
    }

    // ========== FUNCIONES CRUD DE RECETAS ==========

    fun toggleFavorito(receta: Receta) {
        viewModelScope.launch {
            try {
                firebaseController.marcarComoFavorita(receta.id, !receta.esFavorita)
            } catch (e: Exception) {
                _error.value = "Error actualizando favorito: ${e.message}"
            }
        }
    }

    // FUNCIÓN CORREGIDA - ahora imagenUrl es obligatorio
// FUNCIÓN CORREGIDA
    fun agregarReceta(
        nombre: String,
        tiempo: Int,
        ingredientes: List<Ingrediente>,
        pasos: List<String>,
        categoria: String,
        imagenUrl: String
    ) {
        viewModelScope.launch {
            try {
                // VALIDAR QUE LA URL NO ESTÉ VACÍA
                if (imagenUrl.isBlank()) {
                    _error.value = "La URL de la imagen es obligatoria"
                    return@launch
                }

                val nuevaReceta = Receta(
                    nombre = nombre,
                    tiempoPreparacion = tiempo,
                    ingredientes = ingredientes,
                    pasos = pasos,
                    categoria = categoria,
                    esFavorita = false,
                    imagenUrl = imagenUrl.trim()
                )

                // DEBUG: Verificar qué estamos guardando
                println("🆕 DEBUG ViewModel - agregarReceta:")
                println("   - Nombre: $nombre")
                println("   - Tiempo: $tiempo min")
                println("   - Ingredientes: ${ingredientes.size}")
                ingredientes.forEachIndexed { index, ingrediente ->
                    println("   - ${index + 1}. ${ingrediente.nombre}: ${ingrediente.cantidad} ${ingrediente.unidad}")
                }
                println("   - Pasos: ${pasos.size}")
                println("   - Categoría: $categoria")
                println("   - URL: $imagenUrl")

                val idGenerado = firebaseController.insertarReceta(nuevaReceta)
                _error.value = null

                println("✅ Receta agregada a Firestore")
                println("   - ID generado: $idGenerado")

            } catch (e: Exception) {
                println("❌ Error en ViewModel agregando receta: ${e.message}")
                _error.value = "Error agregando receta: ${e.message}"
            }
        }
    }

    fun recargarDatos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                firebaseController.cargarDatosIniciales()
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
                firebaseController.eliminarReceta(receta.id)
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
                    firebaseController.eliminarReceta(receta.id)
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error eliminando todas las recetas: ${e.message}"
            }
        }
    }

    // ========== FUNCIONES DE PERFILES DE USUARIO ==========

    fun crearNuevoPerfil(nombre: String, correo: String) {
        val nuevoUsuario = Usuario(
            id = System.currentTimeMillis(),
            nombre = nombre,
            correo = correo,
            contraseña = "", // No necesitamos contraseña en este sistema
            recetasGuardadas = emptyList(),
            historialBusquedas = emptyList()
        )

        _perfilesUsuarios.value = _perfilesUsuarios.value + nuevoUsuario
        _perfilActivo.value = nuevoUsuario
    }

    fun seleccionarPerfil(usuario: Usuario) {
        _perfilActivo.value = usuario
    }

    fun eliminarPerfil(usuario: Usuario) {
        _perfilesUsuarios.value = _perfilesUsuarios.value - usuario
        // Si el usuario eliminado era el activo, seleccionar otro o dejar null
        if (_perfilActivo.value?.id == usuario.id) {
            _perfilActivo.value = _perfilesUsuarios.value.firstOrNull()
        }
    }

    fun obtenerPerfilActivo(): Usuario? {
        return _perfilActivo.value
    }

    // Cargar algunos usuarios de ejemplo al inicio
    private fun cargarUsuariosEjemplo() {
        val usuariosEjemplo = listOf(
            Usuario(
                id = 1,
                nombre = "Chef María",
                correo = "maria@cocina.com",
                contraseña = "",
                recetasGuardadas = emptyList(),
                historialBusquedas = emptyList()
            ),
            Usuario(
                id = 2,
                nombre = "Cocinero Juan",
                correo = "juan@recetas.com",
                contraseña = "",
                recetasGuardadas = emptyList(),
                historialBusquedas = emptyList()
            ),
            Usuario(
                id = 3,
                nombre = "Ana Postres",
                correo = "ana@dulces.com",
                contraseña = "",
                recetasGuardadas = emptyList(),
                historialBusquedas = emptyList()
            )
        )

        _perfilesUsuarios.value = usuariosEjemplo
        _perfilActivo.value = usuariosEjemplo.first()
    }

    // ========== FUNCIONES DE UTILIDAD ==========

    fun obtenerRecetaPorId(id: String): Receta? {
        return recetas.value.find { it.id == id }
    }

    fun obtenerRecetasPorCategoria(categoria: String): List<Receta> {
        return recetas.value.filter { it.categoria.equals(categoria, ignoreCase = true) }
    }

    fun contarRecetas(): Int {
        return recetas.value.size
    }

    fun contarFavoritas(): Int {
        return recetasFavoritas.value.size
    }

    fun tieneRecetas(): Boolean {
        return recetas.value.isNotEmpty()
    }

    fun tieneFavoritas(): Boolean {
        return recetasFavoritas.value.isNotEmpty()
    }

    fun limpiarErrores() {
        _error.value = null
    }
}