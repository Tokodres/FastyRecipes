package com.example.fastyrecipes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.fastyrecipes.controller.FastyController
import com.example.fastyrecipes.ui.pantallas.PantallaPrincipal
import com.example.fastyrecipes.ui.theme.FastyRecipesTheme
import com.example.fastyrecipes.viewmodels.RecetasViewModel
import com.example.fastyrecipes.viewmodels.RecetasViewModelFactory

class MainActivity : ComponentActivity() {

    // Instancia del controller
    private val controller: FastyController by lazy {
        FastyController(applicationContext)
    }

    // ViewModel con Factory
    private val viewModel: RecetasViewModel by viewModels {
        RecetasViewModelFactory(controller)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FastyRecipesTheme {
                PantallaPrincipal(viewModel = viewModel)
            }
        }
    }
}