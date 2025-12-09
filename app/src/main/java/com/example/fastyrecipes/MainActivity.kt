package com.example.fastyrecipes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fastyrecipes.controller.FirebaseAuthController
import com.example.fastyrecipes.controller.FirebaseController
import com.example.fastyrecipes.modelo.Receta
import com.example.fastyrecipes.ui.pantallas.*
import com.example.fastyrecipes.ui.theme.FastyRecipesTheme
import com.example.fastyrecipes.viewmodels.FirebaseViewModelFactory
import com.example.fastyrecipes.viewmodels.RecetasViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val firebaseController: FirebaseController by lazy {
        FirebaseController()
    }

    private val firebaseAuthController: FirebaseAuthController by lazy {
        FirebaseAuthController()
    }

    private val viewModel: RecetasViewModel by viewModels {
        FirebaseViewModelFactory(firebaseController, firebaseAuthController)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FastyRecipesTheme {
                AppNavigation(viewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: RecetasViewModel) {
    val estaAutenticado by viewModel.estaAutenticado.collectAsStateWithLifecycle()
    val esInvitado by viewModel.esInvitado.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf("cargando") }
    var selectedReceta by remember { mutableStateOf<Receta?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }

    // Debug: Ver estados
    println("🔍 AppNavigation - currentScreen: $currentScreen")
    println("🔍 AppNavigation - estaAutenticado: $estaAutenticado")
    println("🔍 AppNavigation - esInvitado: $esInvitado")
    println("🔍 AppNavigation - isLoading: $isLoading")

    // Manejar estado de carga inicial
    if (estaAutenticado == null || isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Navegación inicial - SOLO cuando cambia de null a un valor concreto
    LaunchedEffect(estaAutenticado) {
        println("🎯 LaunchedEffect - estaAutenticado: $estaAutenticado, currentScreen: $currentScreen")
        if (currentScreen == "cargando" && estaAutenticado != null) {
            currentScreen = if (estaAutenticado!!) "inicio" else "autenticacion"
            println("➡️ Navegando a: $currentScreen")
        }
    }

    // Observar cambios en el estado de autenticación para navegar automáticamente
    LaunchedEffect(estaAutenticado, error) {
        if (estaAutenticado == true && currentScreen == "autenticacion") {
            // Esperar un momento para asegurar que la UI se actualice
            delay(500)
            println("✅ Autenticación exitosa, navegando a inicio")
            currentScreen = "inicio"
            authError = null // Limpiar errores anteriores
        } else if (error != null && currentScreen == "autenticacion") {
            // Mostrar error en pantalla de autenticación
            authError = error
            println("❌ Error de autenticación: $error")
        }
    }

    // Si hay error, mostrarlo en consola
    LaunchedEffect(error) {
        error?.let {
            println("❌ Error general: $it")
        }
    }

    // Renderizar pantalla actual
    println("🎨 Renderizando pantalla: $currentScreen")
    when (currentScreen) {
        "cargando" -> {
            // Ya se maneja arriba con el loading screen
        }

        "autenticacion" -> {
            println("👁️ Mostrando PantallaAutenticacion")
            PantallaAutenticacion(
                onIniciarSesion = { correo, contraseña ->
                    println("🔑 Iniciando sesión con: $correo")
                    authError = null // Limpiar errores anteriores
                    viewModel.iniciarSesion(correo, contraseña)
                    // NO navegar inmediatamente - esperar a que el ViewModel actualice el estado
                },
                onRegistrarse = { nombre, correo, contraseña ->
                    println("📝 Registrando: $nombre, $correo")
                    authError = null // Limpiar errores anteriores
                    viewModel.registrarUsuario(nombre, correo, contraseña)
                    // NO navegar inmediatamente - esperar a que el ViewModel actualice el estado
                },
                onIniciarComoInvitado = {
                    println("👤 Iniciando como invitado")
                    viewModel.iniciarComoInvitado()
                    // Para invitado, navegar inmediatamente
                    currentScreen = "inicio"
                },
                isLoading = isLoading,
                errorMessage = authError ?: error // Mostrar error de autenticación si existe
            )
        }

        "inicio" -> {
            println("👁️ Mostrando PantallaPrincipal")
            PantallaPrincipal(
                viewModel = viewModel,
                onNavigateToInicio = {
                    println("🏠 Navegando a inicio")
                    currentScreen = "inicio"
                },
                onNavigateToSearch = {
                    println("🔍 Navegando a búsqueda")
                    currentScreen = "buscar"
                },
                onNavigateToFavoritos = {
                    println("❤️ Navegando a favoritos")
                    currentScreen = "favoritos"
                },
                onNavigateToPerfil = {
                    if (esInvitado) {
                        println("👤 Invitado navegando a perfil - redirigiendo a autenticación")
                        currentScreen = "autenticacion"
                    } else {
                        println("👤 Navegando a perfil")
                        currentScreen = "perfil"
                    }
                },
                onNavigateToCrearReceta = {
                    if (esInvitado) {
                        println("🚫 Invitado intentando crear receta - redirigiendo a autenticación")
                        currentScreen = "autenticacion"
                    } else {
                        println("➕ Navegando a crear receta")
                        currentScreen = "crearReceta"
                    }
                },
                onNavigateToDetalleReceta = { receta ->
                    println("📄 Navegando a detalle de receta: ${receta.nombre}")
                    selectedReceta = receta
                    currentScreen = "detalle"
                }
            )
        }

        "buscar" -> {
            println("👁️ Mostrando PantallaBusqueda")
            PantallaBusqueda(
                viewModel = viewModel,
                onBack = {
                    println("↩️ Volviendo desde búsqueda")
                    currentScreen = "inicio"
                },
                onNavigateToDetalleReceta = { receta ->
                    println("📄 Navegando a detalle de receta desde búsqueda: ${receta.nombre}")
                    selectedReceta = receta
                    currentScreen = "detalle"
                }
            )
        }

        "favoritos" -> {
            println("👁️ Mostrando PantallaFavoritos")
            PantallaFavoritos(
                viewModel = viewModel,
                onBack = {
                    println("↩️ Volviendo desde favoritos")
                    currentScreen = "inicio"
                },
                onNavigateToInicio = { currentScreen = "inicio" },
                onNavigateToSearch = { currentScreen = "buscar" },
                onNavigateToFavoritos = { currentScreen = "favoritos" },
                onNavigateToPerfil = {
                    if (esInvitado) {
                        currentScreen = "autenticacion"
                    } else {
                        currentScreen = "perfil"
                    }
                },
                onNavigateToDetalleReceta = { receta ->
                    selectedReceta = receta
                    currentScreen = "detalle"
                }
            )
        }

        "perfil" -> {
            println("👁️ Mostrando PantallaPerfil")
            PantallaPerfil(
                viewModel = viewModel,
                onBack = {
                    println("↩️ Volviendo desde perfil")
                    currentScreen = "inicio"
                },
                onNavigateToInicio = { currentScreen = "inicio" },
                onNavigateToSearch = { currentScreen = "buscar" },
                onNavigateToFavoritos = { currentScreen = "favoritos" },
                onNavigateToPerfil = { currentScreen = "perfil" },
                onCerrarSesion = {
                    println("🚪 Cerrando sesión")
                    viewModel.cerrarSesion()
                    currentScreen = "autenticacion"
                }
            )
        }

        "crearReceta" -> {
            println("👁️ Mostrando PantallaCrearReceta")
            PantallaCrearReceta(
                onGuardar = { nombre, tiempo, ingredientes, pasos, categoria, imagenUrl ->
                    println("💾 Guardando receta: $nombre")
                    viewModel.agregarReceta(
                        nombre = nombre,
                        tiempo = tiempo,
                        ingredientes = ingredientes,
                        pasos = pasos,
                        categoria = categoria,
                        imagenUrl = imagenUrl
                    )
                    currentScreen = "inicio"
                },
                onCancelar = {
                    println("❌ Cancelando creación de receta")
                    currentScreen = "inicio"
                }
            )
        }

        "detalle" -> {
            println("👁️ Mostrando PantallaDetalleReceta")
            selectedReceta?.let { receta ->
                PantallaDetalleReceta(
                    receta = receta,
                    onBack = {
                        println("↩️ Volviendo desde detalle")
                        currentScreen = "inicio"
                    },
                    onToggleFavorito = {
                        println("⭐ Toggle favorito para: ${receta.nombre}")
                        viewModel.toggleFavorito(receta)
                    }
                )
            } ?: run {
                println("⚠️ No hay receta seleccionada, volviendo a inicio")
                currentScreen = "inicio"
            }
        }
    }
}