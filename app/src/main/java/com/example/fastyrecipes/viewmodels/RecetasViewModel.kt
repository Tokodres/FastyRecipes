package com.example.fastyrecipes.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastyrecipes.controller.FirebaseAuthController
import com.example.fastyrecipes.controller.FirebaseController
import com.example.fastyrecipes.modelo.Receta
import com.example.fastyrecipes.modelo.Usuario
import com.example.fastyrecipes.modelo.Ingrediente
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RecetasViewModel(
    private val firebaseController: FirebaseController,
    private val firebaseAuthController: FirebaseAuthController
) : ViewModel() {

    // ========== ESTADOS DE AUTENTICACIÓN ==========
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _estaAutenticado = MutableStateFlow<Boolean?>(null)  // Cambiar a nullable
    val estaAutenticado: StateFlow<Boolean?> = _estaAutenticado.asStateFlow()

    private val _esInvitado = MutableStateFlow(false)
    val esInvitado: StateFlow<Boolean> = _esInvitado.asStateFlow()

    // ========== ESTADOS DE RECETAS ==========
    // Flow que observa cambios en tiempo real de Firebase Firestore
    val recetas = firebaseController.obtenerTodasLasRecetas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Estados de carga y error
    private val _isLoading = MutableStateFlow(false)
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

    // Recetas favoritas del usuario actual
    val recetasFavoritas: StateFlow<List<Receta>> = combine(
        recetas,
        _usuarioActual,
        _esInvitado
    ) { recetas, usuario, esInvitado ->
        if (!esInvitado && usuario != null) {
            // Si está autenticado, usar sus favoritos
            recetas.filter { receta ->
                usuario.recetasGuardadas.contains(receta.id)
            }
        } else {
            // Si es invitado, usar el campo esFavorita local
            recetas.filter { it.esFavorita }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        println("🟢 RecetasViewModel - Inicializando...")
        verificarAutenticacionInicial()
        cargarDatosIniciales()
    }

    private fun verificarAutenticacionInicial() {
        viewModelScope.launch {
            println("🟡 RecetasViewModel - Verificando autenticación inicial...")
            _isLoading.value = true

            try {
                // Usar Firebase Auth real para verificar
                val usuarioFirebase = firebaseAuthController.obtenerUsuarioActual()

                if (usuarioFirebase != null) {
                    // Usuario autenticado con Firebase Auth
                    println("👤 RecetasViewModel - Usuario Firebase encontrado: ${usuarioFirebase.correo}")

                    // Obtener datos completos de Firestore
                    val usuarioCompleto = try {
                        firebaseAuthController.obtenerUsuario(usuarioFirebase.id)
                    } catch (e: Exception) {
                        println("⚠️ No se pudo obtener usuario de Firestore, usando datos básicos")
                        usuarioFirebase
                    }

                    _usuarioActual.value = usuarioCompleto
                    _estaAutenticado.value = true
                    _esInvitado.value = false
                    println("✅ RecetasViewModel - Usuario autenticado: ${usuarioCompleto?.nombre}")
                } else {
                    // No hay usuario autenticado
                    println("👤 RecetasViewModel - No hay usuario autenticado")
                    _estaAutenticado.value = false
                    _esInvitado.value = false
                }

                _error.value = null

            } catch (e: Exception) {
                println("❌ RecetasViewModel - Error en verificarAutenticacionInicial: ${e.message}")
                _error.value = "Error verificando autenticación: ${e.message}"
                _estaAutenticado.value = false

            } finally {
                _isLoading.value = false
                println("🔵 RecetasViewModel - Estado final: estaAutenticado=${_estaAutenticado.value}")
            }
        }
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            try {
                println("📥 RecetasViewModel - Cargando datos iniciales...")
                firebaseController.cargarDatosIniciales()
                _error.value = null
            } catch (e: Exception) {
                println("❌ RecetasViewModel - Error cargando datos iniciales: ${e.message}")
                _error.value = "Error inicializando datos: ${e.message}"
            }
        }
    }

    // ========== FUNCIONES DE AUTENTICACIÓN ==========

    fun registrarUsuario(nombre: String, correo: String, contraseña: String) {
        viewModelScope.launch {
            try {
                println("📝 RecetasViewModel - Registrando usuario: $correo")
                _isLoading.value = true
                _error.value = null

                // Usar Firebase Auth real
                val resultado = firebaseAuthController.registrarUsuario(correo, contraseña, nombre)

                if (resultado.isSuccess) {
                    val usuario = resultado.getOrThrow()
                    println("✅ RecetasViewModel - Usuario registrado exitosamente: ${usuario.nombre}")
                    _usuarioActual.value = usuario
                    _estaAutenticado.value = true
                    _esInvitado.value = false
                    _error.value = null
                } else {
                    val errorMsg = resultado.exceptionOrNull()?.message ?: "Error desconocido"
                    println("❌ RecetasViewModel - Error al registrar: $errorMsg")
                    _error.value = errorMsg
                }

            } catch (e: Exception) {
                println("❌ RecetasViewModel - Error en registrarUsuario: ${e.message}")
                _error.value = e.message ?: "Error al registrar usuario"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun iniciarSesion(correo: String, contraseña: String) {
        viewModelScope.launch {
            try {
                println("🔑 RecetasViewModel - Iniciando sesión para: $correo")
                _isLoading.value = true
                _error.value = null  // Limpiar errores anteriores

                // Usar Firebase Auth real
                val resultado = firebaseAuthController.iniciarSesion(correo, contraseña)

                if (resultado.isSuccess) {
                    val usuario = resultado.getOrThrow()
                    println("✅ RecetasViewModel - Inicio de sesión exitoso: ${usuario.nombre}")
                    _usuarioActual.value = usuario
                    _estaAutenticado.value = true
                    _esInvitado.value = false
                    _error.value = null  // Asegurar que no hay error
                } else {
                    val errorMsg = resultado.exceptionOrNull()?.message ?: "Credenciales incorrectas"
                    println("❌ RecetasViewModel - Error al iniciar sesión: $errorMsg")
                    _error.value = errorMsg
                }

            } catch (e: Exception) {
                println("❌ RecetasViewModel - Error en iniciarSesion: ${e.message}")
                _error.value = e.message ?: "Error al iniciar sesión"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun iniciarComoInvitado() {
        println("👤 RecetasViewModel - Iniciando como invitado")
        _usuarioActual.value = Usuario(
            id = "invitado_${System.currentTimeMillis()}",
            nombre = "Invitado",
            correo = "",
            contraseña = "",
            fechaRegistro = System.currentTimeMillis(),
            recetasGuardadas = emptyList(),
            historialBusquedas = emptyList()
        )
        _estaAutenticado.value = true  // Para navegación, está "autenticado" como invitado
        _esInvitado.value = true
        _error.value = null
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            try {
                println("🚪 RecetasViewModel - Cerrando sesión...")
                _isLoading.value = true

                if (!_esInvitado.value) {
                    firebaseAuthController.cerrarSesion()
                }

                // Limpiar estados
                _usuarioActual.value = null
                _estaAutenticado.value = false
                _esInvitado.value = false
                _error.value = null

                println("✅ RecetasViewModel - Sesión cerrada exitosamente")

            } catch (e: Exception) {
                println("❌ RecetasViewModel - Error en cerrarSesion: ${e.message}")
                _error.value = "Error al cerrar sesión: ${e.message}"
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

    fun toggleFavorito(receta: Receta) {
        viewModelScope.launch {
            try {
                if (_esInvitado.value) {
                    // Para invitados: cambiar el estado localmente
                    firebaseController.marcarComoFavorita(receta.id, !receta.esFavorita)
                } else {
                    // Para usuarios autenticados: actualizar en su perfil
                    val usuario = _usuarioActual.value
                    if (usuario != null) {
                        val nuevasRecetasGuardadas = if (usuario.recetasGuardadas.contains(receta.id)) {
                            usuario.recetasGuardadas - receta.id
                        } else {
                            usuario.recetasGuardadas + receta.id
                        }

                        val usuarioActualizado = usuario.copy(recetasGuardadas = nuevasRecetasGuardadas)
                        firebaseAuthController.guardarUsuario(usuarioActualizado)
                        _usuarioActual.value = usuarioActualizado
                    }
                }
                _error.value = null
            } catch (e: Exception) {
                println("❌ Error actualizando favorito: ${e.message}")
                _error.value = "Error actualizando favorito: ${e.message}"
            }
        }
    }

    fun limpiarTodosLosFavoritos() {
        viewModelScope.launch {
            try {
                if (_esInvitado.value) {
                    // Para invitados: desmarcar todas como favoritas
                    val todasRecetas = recetas.value
                    todasRecetas.forEach { receta ->
                        if (receta.esFavorita) {
                            firebaseController.marcarComoFavorita(receta.id, false)
                        }
                    }
                } else {
                    // Para usuarios autenticados: limpiar su lista de favoritos
                    val usuario = _usuarioActual.value
                    if (usuario != null) {
                        val usuarioActualizado = usuario.copy(recetasGuardadas = emptyList())
                        firebaseAuthController.guardarUsuario(usuarioActualizado)
                        _usuarioActual.value = usuarioActualizado
                    }
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error limpiando favoritos: ${e.message}"
            }
        }
    }

    // ========== FUNCIONES CRUD DE RECETAS ==========

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
                if (imagenUrl.isBlank()) {
                    _error.value = "La URL de la imagen es obligatoria"
                    return@launch
                }

                val descripcion = if (_esInvitado.value) {
                    "Receta creada por Invitado"
                } else {
                    "Receta creada por ${_usuarioActual.value?.nombre ?: "usuario"}"
                }

                val nuevaReceta = Receta(
                    nombre = nombre,
                    descripcion = descripcion,
                    tiempoPreparacion = tiempo,
                    ingredientes = ingredientes,
                    pasos = pasos,
                    categoria = categoria,
                    esFavorita = false,
                    imagenUrl = imagenUrl.trim()
                )

                println("🆕 RecetasViewModel - Agregando receta: $nombre")

                val idGenerado = firebaseController.insertarReceta(nuevaReceta)
                _error.value = null

                println("✅ RecetasViewModel - Receta agregada exitosamente, ID: $idGenerado")

            } catch (e: Exception) {
                println("❌ RecetasViewModel - Error agregando receta: ${e.message}")
                _error.value = "Error agregando receta: ${e.message}"
            }
        }
    }

    fun recargarDatos() {
        viewModelScope.launch {
            try {
                println("🔄 RecetasViewModel - Recargando datos...")
                _isLoading.value = true
                firebaseController.cargarDatosIniciales()
                _error.value = null
            } catch (e: Exception) {
                println("❌ RecetasViewModel - Error recargando datos: ${e.message}")
                _error.value = "Error recargando datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarReceta(receta: Receta) {
        viewModelScope.launch {
            try {
                println("🗑️ RecetasViewModel - Eliminando receta: ${receta.nombre}")
                firebaseController.eliminarReceta(receta.id)
                _error.value = null
            } catch (e: Exception) {
                println("❌ RecetasViewModel - Error eliminando receta: ${e.message}")
                _error.value = "Error eliminando receta: ${e.message}"
            }
        }
    }

    fun eliminarTodasLasRecetas() {
        viewModelScope.launch {
            try {
                println("🗑️ RecetasViewModel - Eliminando todas las recetas...")
                val todasRecetas = recetas.value
                todasRecetas.forEach { receta ->
                    firebaseController.eliminarReceta(receta.id)
                }
                _error.value = null
            } catch (e: Exception) {
                println("❌ RecetasViewModel - Error eliminando todas las recetas: ${e.message}")
                _error.value = "Error eliminando todas las recetas: ${e.message}"
            }
        }
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

    fun agregarBusquedaAlHistorial(termino: String) {
        viewModelScope.launch {
            try {
                if (!_esInvitado.value) {
                    val usuario = _usuarioActual.value
                    if (usuario != null && !usuario.historialBusquedas.contains(termino)) {
                        val nuevoHistorial = usuario.historialBusquedas + termino
                        val usuarioActualizado = usuario.copy(historialBusquedas = nuevoHistorial)
                        firebaseAuthController.guardarUsuario(usuarioActualizado)
                        _usuarioActual.value = usuarioActualizado
                    }
                }
            } catch (e: Exception) {
                println("❌ RecetasViewModel - Error agregando búsqueda al historial: ${e.message}")
            }
        }
    }

    fun formatearFecha(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    fun obtenerPerfilActivo(): Usuario? {
        return _usuarioActual.value
    }
}