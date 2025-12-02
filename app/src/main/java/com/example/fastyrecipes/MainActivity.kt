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
import com.example.fastyrecipes.ui.pantallas.PantallaBusqueda
import com.example.fastyrecipes.ui.pantallas.PantallaFavoritos
import com.example.fastyrecipes.ui.pantallas.PantallaPerfil
import com.example.fastyrecipes.ui.pantallas.PantallaPrincipal
import com.example.fastyrecipes.ui.theme.FastyRecipesTheme
import com.example.fastyrecipes.viewmodels.RecetasViewModel
import com.example.fastyrecipes.viewmodels.FirebaseViewModelFactory
import kotlinx.coroutines.launch

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

    // Cargar datos iniciales cuando la app inicia
    LaunchedEffect(Unit) {
        viewModel.recargarDatos()
    }

    when (currentScreen) {
        "inicio" -> PantallaPrincipal(
            viewModel = viewModel,
            onNavigateToInicio = { currentScreen = "inicio" },
            onNavigateToSearch = { currentScreen = "buscar" },
            onNavigateToFavoritos = { currentScreen = "favoritos" },
            onNavigateToPerfil = { currentScreen = "perfil" }
        )
        "buscar" -> PantallaBusqueda(
            viewModel = viewModel,
            onBack = { currentScreen = "inicio" }
        )
        "favoritos" -> PantallaFavoritos(
            viewModel = viewModel,
            onBack = { currentScreen = "inicio" },
            onNavigateToInicio = { currentScreen = "inicio" },
            onNavigateToSearch = { currentScreen = "buscar" },
            onNavigateToFavoritos = { currentScreen = "favoritos" },
            onNavigateToPerfil = { currentScreen = "perfil" }
        )
        "perfil" -> PantallaPerfil(
            viewModel = viewModel,
            onBack = { currentScreen = "inicio" },
            onNavigateToInicio = { currentScreen = "inicio" },
            onNavigateToSearch = { currentScreen = "buscar" },
            onNavigateToFavoritos = { currentScreen = "favoritos" },
            onNavigateToPerfil = { currentScreen = "perfil" }
        )
    }
}