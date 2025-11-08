package com.example.fastyrecipes.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fastyrecipes.modelo.Receta
import com.example.fastyrecipes.ui.components.BottomNavigationBar
import com.example.fastyrecipes.ui.theme.FastyRecipesTheme
import com.example.fastyrecipes.viewmodels.RecetasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(
    viewModel: RecetasViewModel,
    onNavigateToSearch: () -> Unit
) {

    val recetas by viewModel.recetas.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Fasty Recipes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentScreen = "inicio",
                onNavigateToSearch = onNavigateToSearch
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar receta")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Botón para recargar datos (restaurado)
            Button(
                onClick = { viewModel.recargarDatos() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recargar Datos")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Estado de carga
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Contenido principal
                if (recetas.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hay recetas disponibles",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    // Lista de recetas con diseño original
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(recetas) { receta ->
                            RecetaItemOriginal(
                                receta = receta,
                                onToggleFavorito = { viewModel.toggleFavorito(receta) },
                                onEliminar = { viewModel.eliminarReceta(receta) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Mostrar errores
        error?.let { errorMessage ->
            LaunchedEffect(errorMessage) {
                // Aquí podrías mostrar un Snackbar
            }
        }

        // Dialog para agregar receta
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Agregar Nueva Receta") },
                text = {
                    AgregarRecetaForm { nombre, descripcion, tiempo, categoria ->
                        viewModel.agregarReceta(nombre, descripcion, tiempo, categoria)
                        showAddDialog = false
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

// Componente original de RecetaItem (con eliminar y favoritos)
@Composable
fun RecetaItemOriginal(
    receta: Receta,
    onToggleFavorito: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = receta.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = receta.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tiempo: ${receta.tiempoPreparacion} min")
                Text("Categoría: ${receta.categoria}")
            }
            Row {
                // Botón de favorito
                IconButton(onClick = onToggleFavorito) {
                    Icon(
                        if (receta.esFavorita) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (receta.esFavorita) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                // Botón de eliminar
                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AgregarRecetaForm(onAgregar: (String, String, Int, String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tiempo by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = tiempo,
            onValueChange = { tiempo = it },
            label = { Text("Tiempo (minutos)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = categoria,
            onValueChange = { categoria = it },
            label = { Text("Categoría") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val tiempoInt = tiempo.toIntOrNull() ?: 0
                if (nombre.isNotEmpty() && descripcion.isNotEmpty() && tiempoInt > 0 && categoria.isNotEmpty()) {
                    onAgregar(nombre, descripcion, tiempoInt, categoria)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar Receta")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaPrincipal() {
    FastyRecipesTheme {
        // PantallaPrincipal(viewModel = ...)
    }
}