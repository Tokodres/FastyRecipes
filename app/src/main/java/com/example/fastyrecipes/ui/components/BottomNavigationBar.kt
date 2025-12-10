package com.example.fastyrecipes.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fastyrecipes.viewmodels.RecetasViewModel

@Composable
fun BottomNavigationBar(
    viewModel: RecetasViewModel,
    currentScreen: String,
    onNavigateToInicio: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFavoritos: () -> Unit,
    onNavigateToPerfil: () -> Unit
) {
    // Usa collectAsState() en lugar de collectAsStateWithLifecycle()
    val textosTraducidos = viewModel.textosTraducidos.collectAsState().value

    fun texto(key: String): String {
        return textosTraducidos[key] ?: key
    }

    NavigationBar(
        modifier = Modifier.height(70.dp)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = texto("inicio")) },
            label = { Text(texto("inicio")) },
            selected = currentScreen == "inicio",
            onClick = onNavigateToInicio
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = texto("buscar")) },
            label = { Text(texto("buscar")) },
            selected = currentScreen == "buscar",
            onClick = onNavigateToSearch
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = texto("favoritos")) },
            label = { Text(texto("favoritos")) },
            selected = currentScreen == "favoritos",
            onClick = onNavigateToFavoritos
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = texto("perfil")) },
            label = { Text(texto("perfil")) },
            selected = currentScreen == "perfil",
            onClick = onNavigateToPerfil
        )
    }
}