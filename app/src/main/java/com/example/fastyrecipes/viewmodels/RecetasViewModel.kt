package com.example.fastyrecipes.viewmodels

import android.content.Context
import android.content.SharedPreferences
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

    private val _estaAutenticado = MutableStateFlow<Boolean?>(null)
    val estaAutenticado: StateFlow<Boolean?> = _estaAutenticado.asStateFlow()

    private val _esInvitado = MutableStateFlow(false)
    val esInvitado: StateFlow<Boolean> = _esInvitado.asStateFlow()

    // ========== ESTADO DE IDIOMA ==========
    private val _idiomaActual = MutableStateFlow("Español")
    val idiomaActual: StateFlow<String> = _idiomaActual.asStateFlow()

    private val _idiomasDisponibles = MutableStateFlow(
        listOf(
            "Español" to "es",
            "English" to "en"
        )
    )
    val idiomasDisponibles: StateFlow<List<Pair<String, String>>> = _idiomasDisponibles.asStateFlow()

    // ========== TEXTO TRADUCIDO ==========
    private val _textosTraducidos = MutableStateFlow<Map<String, String>>(emptyMap())
    val textosTraducidos: StateFlow<Map<String, String>> = _textosTraducidos.asStateFlow()

    // ========== ESTADOS DE RECETAS ==========
    val recetas = firebaseController.obtenerTodasLasRecetas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

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

    val categoriasUnicas: StateFlow<List<String>> = recetas.map { recetas ->
        recetas.map { it.categoria }.distinct()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recetasFavoritas: StateFlow<List<Receta>> = combine(
        recetas,
        _usuarioActual,
        _esInvitado
    ) { recetas, usuario, esInvitado ->
        if (!esInvitado && usuario != null) {
            recetas.filter { receta ->
                usuario.recetasGuardadas.contains(receta.id)
            }
        } else {
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
        // Cargaremos el idioma cuando tengamos contexto en MainActivity
    }

    private fun verificarAutenticacionInicial() {
        viewModelScope.launch {
            println("🟡 RecetasViewModel - Verificando autenticación inicial...")
            _isLoading.value = true

            try {
                val usuarioFirebase = firebaseAuthController.obtenerUsuarioActual()

                if (usuarioFirebase != null) {
                    println("👤 RecetasViewModel - Usuario Firebase encontrado: ${usuarioFirebase.correo}")

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

    // ========== FUNCIONES DE IDIOMA ==========

    fun cambiarIdioma(nombreIdioma: String, context: Context) {
        viewModelScope.launch {
            try {
                println("🌐 RecetasViewModel - Cambiando idioma a: $nombreIdioma")

                // Actualizar el estado del idioma actual
                _idiomaActual.value = nombreIdioma

                // Cargar traducciones para el nuevo idioma
                cargarTraducciones(nombreIdioma)

                // Guardar en SharedPreferences
                val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                sharedPref.edit()
                    .putString("idioma", if (nombreIdioma == "English") "en" else "es")
                    .apply()
                println("💾 Idioma guardado en SharedPreferences: $nombreIdioma")

                // Si el usuario está autenticado, guardar en Firestore
                if (!_esInvitado.value) {
                    val usuario = _usuarioActual.value
                    if (usuario != null) {
                        try {
                            val idiomaCodigo = if (nombreIdioma == "English") "en" else "es"
                            firebaseAuthController.guardarIdiomaUsuario(usuario.id, idiomaCodigo)
                            println("📝 Idioma guardado para usuario: ${usuario.nombre}")

                            // Actualizar usuario local
                            val usuarioActualizado = usuario.copy(idiomaPreferido = idiomaCodigo)
                            _usuarioActual.value = usuarioActualizado
                        } catch (e: Exception) {
                            println("⚠️ Error guardando idioma en Firestore: ${e.message}")
                        }
                    }
                }

                _error.value = null

            } catch (e: Exception) {
                println("❌ RecetasViewModel - Error cambiando idioma: ${e.message}")
                _error.value = "Error cambiando idioma: ${e.message}"
            }
        }
    }

    fun cargarIdiomaGuardado(context: Context) {
        viewModelScope.launch {
            try {
                val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val codigoIdioma = sharedPref.getString("idioma", "es") ?: "es"

                // Convertir código a nombre
                val nombreIdioma = if (codigoIdioma == "en") "English" else "Español"

                // Actualizar estado
                _idiomaActual.value = nombreIdioma

                // Cargar traducciones
                cargarTraducciones(nombreIdioma)

                println("🌐 Idioma cargado: $nombreIdioma ($codigoIdioma)")

            } catch (e: Exception) {
                println("⚠️ Error cargando idioma: ${e.message}")
                _idiomaActual.value = "Español"
                cargarTraducciones("Español")
            }
        }
    }

    private fun cargarTraducciones(idioma: String) {
        val traducciones = when (idioma) {
            "English" -> mapOf(
                // Textos generales
                "app_name" to "FastyRecipes",
                "app_slogan" to "Discover and share delicious recipes",
                "cerrar" to "Close",
                "guardar" to "Save",
                "cancelar" to "Cancel",
                "volver" to "Back",
                "siguiente" to "Next",
                "buscar" to "Search",
                "filtro" to "Filter",
                "cargando" to "Loading",
                "mostrar_contraseña" to "Show password",
                "terminos_condiciones" to "By signing in you accept our Terms and Conditions",

                // Navegación
                "inicio" to "Home",
                "favoritos" to "Favorites",
                "perfil" to "Profile",

                // Pantalla Principal
                "titulo_recetas" to "Recipes",
                "todas_las_categorias" to "All Categories",
                "sin_recetas" to "No recipes available",
                "agrega_recetas_comenzar" to "Add some recipes to get started",
                "crear_receta" to "Create Recipe",
                "ver_detalle" to "View Details",
                "tienes_recetas" to "You have %d recipes",
                "recargar_datos" to "Reload Data",
                "recargar" to "Reload",

                // Pantalla Búsqueda
                "buscar_recetas" to "Search Recipes",
                "placeholder_buscar" to "Search by name or ingredient...",
                "resultados_busqueda" to "Search Results",
                "sin_resultados" to "No results found",
                "categoria" to "Category",
                "categorias" to "Categories",
                "resultados" to "Results",
                "para_busqueda" to "for \"%s\"",

                // Pantalla Favoritos
                "mis_favoritas" to "My Favorites",
                "no_hay_favoritos" to "No favorites yet",
                "agregar_recetas_favoritos" to "Add recipes to favorites to see them here",
                "eliminar_favoritos" to "Remove All Favorites",
                "confirmar_eliminar_favoritos" to "Remove all favorites?",
                "si" to "Yes",
                "no" to "No",
                "cargando_favoritos" to "Loading your favorites...",
                "explorar_recetas" to "Explore Recipes",
                "tus_recetas_favoritas" to "Your favorite recipes",
                "recetas_guardadas" to "recipes saved",

                // Pantalla Perfil
                "mi_perfil" to "My Profile",
                "mis_estadisticas" to "My Statistics",
                "recetas" to "Recipes",
                "favoritas" to "Favorites",
                "busquedas" to "Searches",
                "configuracion_cuenta" to "Account Settings",
                "configuracion" to "Settings",
                "cambiar_contrasena" to "Change Password",
                "contrasena" to "Password",
                "notificaciones" to "Notifications",
                "cerrar_sesion" to "Sign Out",
                "seleccionar_idioma" to "Select Language",
                "idioma" to "Language",
                "invitado_mensaje1" to "You are using the app as guest",
                "invitado_mensaje2" to "Sign in to save your recipes and preferences",
                "iniciar_sesion" to "Sign In",
                "miembro_desde" to "Member since: ",

                // Pantalla Crear Receta
                "crear_nueva_receta" to "Create New Recipe",
                "crear_receta" to "Create Recipe",
                "nombre_receta" to "Recipe Name",
                "descripcion" to "Description",
                "tiempo_preparacion" to "Preparation Time (minutes)",
                "ingredientes" to "Ingredients",
                "pasos" to "Steps",
                "imagen_url" to "Image URL",
                "agregar_ingrediente" to "Add Ingredient",
                "agregar_paso" to "Add Step",
                "nombre_ingrediente" to "Ingredient Name",
                "cantidad" to "Quantity",
                "texto_paso" to "Step Description",
                "requerido" to "Required",
                "guardar_receta" to "Save Recipe",
                "paso" to "Step",
                "informacion_basica" to "Basic Information",
                "ej_nombre_receta" to "Ex: Baked chicken with potatoes",
                "ej_tiempo" to "Ex: 60",
                "ingredientes_anadidos" to "Ingredients added",
                "anadir" to "Add",
                "ej_ingrediente" to "Ex: Wheat flour",
                "ej_cantidad" to "Ex: 200",
                "eliminar_ingrediente" to "Delete ingredient",
                "sin_ingredientes" to "No ingredients",
                "anade_ingredientes_receta" to "Add your recipe ingredients",
                "pasos_preparacion" to "Preparation Steps",
                "pasos_anadidos" to "Steps added",
                "nuevo_paso" to "New step",
                "ej_paso" to "Ex: Preheat oven to 180°C",
                "anadir_paso" to "Add step",
                "eliminar_paso" to "Delete step",
                "sin_pasos" to "No steps",
                "anade_pasos_preparacion" to "Add preparation steps",
                "categoria_imagen" to "Category and Image",
                "generar_nueva_imagen" to "Generate new image for this category",
                "url_automatica" to "Will be generated automatically based on category",
                "url_generada_automaticamente" to "URL is automatically generated based on category",
                "imagen_por_categoria" to "Image by category",
                "imagen_aleatoria" to "Random image",
                "categorias_populares" to "Popular categories:",
                "vista_previa_imagen" to "Image preview",
                "resumen" to "Summary",
                "nombre" to "Name",
                "tiempo" to "Time",
                "minutos" to "minutes",

                // Pantalla Detalle Receta
                "detalle_receta" to "Recipe Details",
                "agregar_favoritos" to "Add to Favorites",
                "quitar_favoritos" to "Remove from Favorites",
                "preparacion" to "Preparation",
                "sin_ingredientes" to "No ingredients specified",
                "sin_pasos_preparacion" to "No preparation steps available",

                // Pantalla Autenticación
                "autenticacion" to "Authentication",
                "iniciar_sesion_titulo" to "Sign In",
                "registrarse_titulo" to "Sign Up",
                "correo_electronico" to "Email",
                "contraseña" to "Password",
                "nombre_usuario" to "Name",
                "confirmar_contraseña" to "Confirm Password",
                "iniciar_como_invitado" to "Continue as Guest",
                "no_tienes_cuenta" to "Don't have an account?",
                "ya_tienes_cuenta" to "Already have an account?",
                "registrate_aqui" to "Sign up here",
                "inicia_sesion_aqui" to "Sign in here",
                "error_credenciales" to "Invalid credentials",
                "error_registro" to "Registration error",
                "exito_registro" to "Registration successful"
            )
            else -> mapOf( // Español por defecto
                // Textos generales
                "app_name" to "FastyRecipes",
                "app_slogan" to "Descubre y comparte recetas deliciosas",
                "cerrar" to "Cerrar",
                "guardar" to "Guardar",
                "cancelar" to "Cancelar",
                "volver" to "Volver",
                "siguiente" to "Siguiente",
                "buscar" to "Buscar",
                "filtro" to "Filtrar",
                "cargando" to "Cargando",
                "mostrar_contraseña" to "Mostrar contraseña",
                "terminos_condiciones" to "Al iniciar sesión aceptas nuestros Términos y Condiciones",

                // Navegación
                "inicio" to "Inicio",
                "favoritos" to "Favoritos",
                "perfil" to "Perfil",

                // Pantalla Principal
                "titulo_recetas" to "Recetas",
                "todas_las_categorias" to "Todas las categorías",
                "sin_recetas" to "No hay recetas disponibles",
                "agrega_recetas_comenzar" to "Agrega algunas recetas para comenzar",
                "crear_receta" to "Crear Receta",
                "ver_detalle" to "Ver Detalles",
                "tienes_recetas" to "Tienes %d recetas",
                "recargar_datos" to "Recargar Datos",
                "recargar" to "Recargar",

                // Pantalla Búsqueda
                "buscar_recetas" to "Buscar Recetas",
                "placeholder_buscar" to "Buscar por nombre o ingrediente...",
                "resultados_busqueda" to "Resultados de búsqueda",
                "sin_resultados" to "No se encontraron resultados",
                "categoria" to "Categoría",
                "categorias" to "Categorías",
                "resultados" to "Resultados",
                "para_busqueda" to "para \"%s\"",

                // Pantalla Favoritos
                "mis_favoritas" to "Mis Favoritas",
                "no_hay_favoritos" to "No hay favoritos aún",
                "agregar_recetas_favoritos" to "Agrega recetas a favoritos para verlas aquí",
                "eliminar_favoritos" to "Eliminar Todos los Favoritos",
                "confirmar_eliminar_favoritos" to "¿Eliminar todos los favoritos?",
                "si" to "Sí",
                "no" to "No",
                "cargando_favoritos" to "Cargando tus favoritos...",
                "explorar_recetas" to "Explorar Recetas",
                "tus_recetas_favoritas" to "Tus recetas favoritas",
                "recetas_guardadas" to "recetas guardadas",

                // Pantalla Perfil
                "mi_perfil" to "Mi Perfil",
                "mis_estadisticas" to "Mis Estadísticas",
                "recetas" to "Recetas",
                "favoritas" to "Favoritas",
                "busquedas" to "Búsquedas",
                "configuracion_cuenta" to "Configuración de la cuenta",
                "configuracion" to "Configuración",
                "cambiar_contrasena" to "Cambiar contraseña",
                "contrasena" to "Contraseña",
                "notificaciones" to "Notificaciones",
                "cerrar_sesion" to "Cerrar Sesión",
                "seleccionar_idioma" to "Seleccionar idioma",
                "idioma" to "Idioma",
                "invitado_mensaje1" to "Estás usando la aplicación como invitado",
                "invitado_mensaje2" to "Inicia sesión para guardar tus recetas y preferencias",
                "iniciar_sesion" to "Iniciar Sesión",
                "miembro_desde" to "Miembro desde: ",

                // Pantalla Crear Receta
                "crear_nueva_receta" to "Crear Nueva Receta",
                "crear_receta" to "Crear Receta",
                "nombre_receta" to "Nombre de la Receta",
                "descripcion" to "Descripción",
                "tiempo_preparacion" to "Tiempo de Preparación (minutos)",
                "ingredientes" to "Ingredientes",
                "pasos" to "Pasos",
                "imagen_url" to "URL de la Imagen",
                "agregar_ingrediente" to "Agregar Ingrediente",
                "agregar_paso" to "Agregar Paso",
                "nombre_ingrediente" to "Nombre del Ingrediente",
                "cantidad" to "Cantidad",
                "texto_paso" to "Descripción del Paso",
                "requerido" to "Requerido",
                "guardar_receta" to "Guardar Receta",
                "paso" to "Paso",
                "informacion_basica" to "Información Básica",
                "ej_nombre_receta" to "Ej: Pollo al horno con patatas",
                "ej_tiempo" to "Ej: 60",
                "ingredientes_anadidos" to "Ingredientes añadidos",
                "anadir" to "Añadir",
                "ej_ingrediente" to "Ej: Harina de trigo",
                "ej_cantidad" to "Ej: 200",
                "eliminar_ingrediente" to "Eliminar ingrediente",
                "sin_ingredientes" to "Sin ingredientes",
                "anade_ingredientes_receta" to "Añade los ingredientes de tu receta",
                "pasos_preparacion" to "Pasos de Preparación",
                "pasos_anadidos" to "Pasos añadidos",
                "nuevo_paso" to "Nuevo paso",
                "ej_paso" to "Ej: Precalentar el horno a 180°C",
                "anadir_paso" to "Añadir paso",
                "eliminar_paso" to "Eliminar paso",
                "sin_pasos" to "Sin pasos",
                "anade_pasos_preparacion" to "Añade los pasos de preparación",
                "categoria_imagen" to "Categoría e Imagen",
                "generar_nueva_imagen" to "Generar nueva imagen para esta categoría",
                "url_automatica" to "Se generará automáticamente basado en la categoría",
                "url_generada_automaticamente" to "La URL se genera automáticamente basada en la categoría",
                "imagen_por_categoria" to "Imagen por categoría",
                "imagen_aleatoria" to "Imagen aleatoria",
                "categorias_populares" to "Categorías populares:",
                "vista_previa_imagen" to "Vista previa de la imagen",
                "resumen" to "Resumen",
                "nombre" to "Nombre",
                "tiempo" to "Tiempo",

                // Pantalla Detalle Receta
                "detalle_receta" to "Detalles de la Receta",
                "agregar_favoritos" to "Agregar a Favoritos",
                "quitar_favoritos" to "Quitar de Favoritos",
                "preparacion" to "Preparación",
                "sin_ingredientes" to "No se especificaron ingredientes",
                "sin_pasos_preparacion" to "No hay pasos de preparación disponibles",

                // Pantalla Autenticación
                "autenticacion" to "Autenticación",
                "iniciar_sesion_titulo" to "Iniciar Sesión",
                "registrarse_titulo" to "Registrarse",
                "correo_electronico" to "Correo Electrónico",
                "contraseña" to "Contraseña",
                "nombre_usuario" to "Nombre",
                "confirmar_contraseña" to "Confirmar Contraseña",
                "iniciar_como_invitado" to "Continuar como Invitado",
                "no_tienes_cuenta" to "¿No tienes una cuenta?",
                "ya_tienes_cuenta" to "¿Ya tienes una cuenta?",
                "registrate_aqui" to "Regístrate aquí",
                "inicia_sesion_aqui" to "Inicia sesión aquí",
                "error_credenciales" to "Credenciales inválidas",
                "error_registro" to "Error en el registro",
                "exito_registro" to "Registro exitoso"
            )
        }
        _textosTraducidos.value = traducciones
    }

    // Función para obtener texto traducido (opcional, se puede usar en lugar de la función local en Composable)
    fun obtenerTexto(key: String): String {
        return _textosTraducidos.value[key] ?: key
    }

    // ========== FUNCIONES DE AUTENTICACIÓN ==========

    fun registrarUsuario(nombre: String, correo: String, contraseña: String) {
        viewModelScope.launch {
            try {
                println("📝 RecetasViewModel - Registrando usuario: $correo")
                _isLoading.value = true
                _error.value = null

                val resultado = firebaseAuthController.registrarUsuario(correo, contraseña, nombre)

                if (resultado.isSuccess) {
                    val usuario = resultado.getOrThrow()
                    println("✅ RecetasViewModel - Usuario registrado exitosamente: ${usuario.nombre}")
                    _usuarioActual.value = usuario
                    _estaAutenticado.value = true
                    _esInvitado.value = false
                    _error.value = null
                    _idiomaActual.value = "Español"

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

    fun iniciarSesion(correo: String, contraseña: String, context: Context) {
        viewModelScope.launch {
            try {
                println("🔑 RecetasViewModel - Iniciando sesión para: $correo")
                _isLoading.value = true
                _error.value = null

                val resultado = firebaseAuthController.iniciarSesion(correo, contraseña)

                if (resultado.isSuccess) {
                    val usuario = resultado.getOrThrow()
                    println("✅ RecetasViewModel - Inicio de sesión exitoso: ${usuario.nombre}")
                    _usuarioActual.value = usuario
                    _estaAutenticado.value = true
                    _esInvitado.value = false
                    _error.value = null

                    // Cargar idioma guardado
                    cargarIdiomaGuardado(context)

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

    fun iniciarComoInvitado(context: Context) {
        println("👤 RecetasViewModel - Iniciando como invitado")
        _usuarioActual.value = Usuario(
            id = "invitado_${System.currentTimeMillis()}",
            nombre = "Invitado",
            correo = "",
            contraseña = "",
            fechaRegistro = System.currentTimeMillis(),
            recetasGuardadas = emptyList(),
            historialBusquedas = emptyList(),
            fotoPerfil = "",
            idiomaPreferido = "es"
        )
        _estaAutenticado.value = true
        _esInvitado.value = true
        _error.value = null

        // Cargar idioma guardado
        cargarIdiomaGuardado(context)
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
                    firebaseController.marcarComoFavorita(receta.id, !receta.esFavorita)
                } else {
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
                    val todasRecetas = recetas.value
                    todasRecetas.forEach { receta ->
                        if (receta.esFavorita) {
                            firebaseController.marcarComoFavorita(receta.id, false)
                        }
                    }
                } else {
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