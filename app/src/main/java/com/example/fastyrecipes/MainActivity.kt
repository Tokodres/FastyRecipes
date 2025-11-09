package com.example.fastyrecipes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import com.example.fastyrecipes.controller.FastyController
import com.example.fastyrecipes.ui.pantallas.PantallaBusqueda
import com.example.fastyrecipes.ui.pantallas.PantallaFavoritos
import com.example.fastyrecipes.ui.pantallas.PantallaPerfil
import com.example.fastyrecipes.ui.pantallas.PantallaPrincipal
import com.example.fastyrecipes.ui.theme.FastyRecipesTheme
import com.example.fastyrecipes.viewmodels.RecetasViewModel
import com.example.fastyrecipes.viewmodels.RecetasViewModelFactory

class MainActivity : ComponentActivity() {

    private val controller: FastyController by lazy {
        FastyController(applicationContext)
    }

    private val viewModel: RecetasViewModel by viewModels {
        RecetasViewModelFactory(controller)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FastyRecipesTheme {
                var currentScreen by remember { mutableStateOf("inicio") }

                when (currentScreen) {
                    "inicio" -> PantallaPrincipal(
                        viewModel = viewModel,
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
                        onNavigateToSearch = { currentScreen = "buscar" },
                        onNavigateToFavoritos = { currentScreen = "favoritos" },
                        onNavigateToPerfil = { currentScreen = "perfil" }
                    )
                    "perfil" -> PantallaPerfil(
                        viewModel = viewModel,
                        onBack = { currentScreen = "inicio" },
                        onNavigateToSearch = { currentScreen = "buscar" },
                        onNavigateToFavoritos = { currentScreen = "favoritos" },
                        onNavigateToPerfil = { currentScreen = "perfil" }
                    )
                }
            }
        }
    }
}