package com.example.fastyrecipes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fastyrecipes.controller.FirebaseController
import com.example.fastyrecipes.ui.pantallas.*
import com.example.fastyrecipes.ui.theme.FastyRecipesTheme
import com.example.fastyrecipes.viewmodels.RecetasViewModel
import com.example.fastyrecipes.viewmodels.FirebaseViewModelFactory

class MainActivity : ComponentActivity() {

    private val firebaseController: FirebaseController by lazy {
        FirebaseController()
    }

    private val viewModel: RecetasViewModel by viewModels {
        FirebaseViewModelFactory(firebaseController)
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
    var currentScreen by remember { mutableStateOf("inicio") }
    var selectedReceta by remember { mutableStateOf<com.example.fastyrecipes.modelo.Receta?>(null) }

    LaunchedEffect(Unit) {
        viewModel.recargarDatos()
    }

    when (currentScreen) {
        "inicio" -> PantallaPrincipal(
            viewModel = viewModel,
            onNavigateToInicio = { currentScreen = "inicio" },
            onNavigateToSearch = { currentScreen = "buscar" },
            onNavigateToFavoritos = { currentScreen = "favoritos" },
            onNavigateToPerfil = { currentScreen = "perfil" },
            onNavigateToCrearReceta = { currentScreen = "crearReceta" },
            onNavigateToDetalleReceta = { receta ->
                selectedReceta = receta
                currentScreen = "detalle"
            }
        )
        "buscar" -> PantallaBusqueda(
            viewModel = viewModel,
            onBack = { currentScreen = "inicio" },
            onNavigateToDetalleReceta = { receta ->
                selectedReceta = receta
                currentScreen = "detalle"
            }
        )
        "favoritos" -> PantallaFavoritos(
            viewModel = viewModel,
            onBack = { currentScreen = "inicio" },
            onNavigateToInicio = { currentScreen = "inicio" },
            onNavigateToSearch = { currentScreen = "buscar" },
            onNavigateToFavoritos = { currentScreen = "favoritos" },
            onNavigateToPerfil = { currentScreen = "perfil" },
            onNavigateToDetalleReceta = { receta ->
                selectedReceta = receta
                currentScreen = "detalle"
            }
        )
        "perfil" -> PantallaPerfil(
            viewModel = viewModel,
            onBack = { currentScreen = "inicio" },
            onNavigateToInicio = { currentScreen = "inicio" },
            onNavigateToSearch = { currentScreen = "buscar" },
            onNavigateToFavoritos = { currentScreen = "favoritos" },
            onNavigateToPerfil = { currentScreen = "perfil" }
        )
        "crearReceta" -> PantallaCrearReceta(
            onGuardar = { nombre, tiempo, ingredientes, pasos, categoria, imagenUrl ->
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
            onCancelar = { currentScreen = "inicio" }
        )
        "detalle" -> {
            selectedReceta?.let { receta ->
                PantallaDetalleReceta(
                    receta = receta,
                    onBack = { currentScreen = "inicio" },
                    onToggleFavorito = { viewModel.toggleFavorito(receta) }
                )
            } ?: run {
                // Si no hay receta seleccionada, volver al inicio
                currentScreen = "inicio"
            }
        }
    }
}