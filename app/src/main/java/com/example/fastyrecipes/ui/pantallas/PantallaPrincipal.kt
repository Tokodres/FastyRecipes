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
import androidx.compose.ui.graphics.Color
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
    onNavigateToInicio: () -> Unit,  // ← NUEVO PARÁMETRO
    onNavigateToSearch: () -> Unit,
    onNavigateToFavoritos: () -> Unit,
    onNavigateToPerfil: () -> Unit
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
                onNavigateToInicio = onNavigateToInicio,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToFavoritos = onNavigateToFavoritos,
                onNavigateToPerfil = onNavigateToPerfil
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
            // Botón para recargar datos
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.FavoriteBorder,
                                contentDescription = "Sin recetas",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No hay recetas disponibles",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Agrega algunas recetas para comenzar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Contador de recetas
                    Text(
                        text = "Tienes ${recetas.size} recetas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Lista de recetas
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
                    AgregarRecetaForm { nombre, descripcion, tiempo, categoria, imagenUrl ->
                        viewModel.agregarReceta(nombre, descripcion, tiempo, categoria, imagenUrl)
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
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (receta.esFavorita) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Imagen de la receta
            com.example.fastyrecipes.ui.components.RecetaImage(
                imageUrl = receta.imagenUrl,
                contentDescription = "Imagen de ${receta.nombre}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Header con nombre y botón de favorito
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = receta.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    // Botón de favorito
                    IconButton(
                        onClick = onToggleFavorito,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            if (receta.esFavorita) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (receta.esFavorita) "Quitar de favoritos" else "Agregar a favoritos",
                            tint = if (receta.esFavorita) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Descripción
                Text(
                    text = receta.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Información de tiempo y categoría
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⏱ ${receta.tiempoPreparacion} min",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "📁 ${receta.categoria}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Botones de acción
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    // Botón de eliminar
                    Button(
                        onClick = onEliminar,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}

@Composable
fun AgregarRecetaForm(onAgregar: (String, String, Int, String, String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tiempo by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var imagenUrl by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre de la receta *") },
            placeholder = { Text("Ej: Pollo al horno") },
            modifier = Modifier.fillMaxWidth(),
            isError = nombre.isEmpty()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción *") },
            placeholder = { Text("Ej: Delicioso pollo horneado con especias") },
            modifier = Modifier.fillMaxWidth(),
            isError = descripcion.isEmpty()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = tiempo,
            onValueChange = { tiempo = it },
            label = { Text("Tiempo de preparación (minutos) *") },
            placeholder = { Text("Ej: 45") },
            modifier = Modifier.fillMaxWidth(),
            isError = tiempo.isEmpty() || tiempo.toIntOrNull() ?: 0 <= 0
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = categoria,
            onValueChange = { categoria = it },
            label = { Text("Categoría *") },
            placeholder = { Text("Ej: Cena, Postre, Ensalada") },
            modifier = Modifier.fillMaxWidth(),
            isError = categoria.isEmpty()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = imagenUrl,
            onValueChange = { imagenUrl = it },
            label = { Text("URL de la imagen *") },
            placeholder = { Text("Ej: https://images.unsplash.com/foto-comida") },
            modifier = Modifier.fillMaxWidth(),
            isError = imagenUrl.isEmpty(),
            supportingText = {
                Text("Usa imágenes de Unsplash (unsplash.com)")
            }
        )

        // Sugerencias de URLs de ejemplo
        Text(
            text = "Sugerencias de URLs:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Botones con URLs de ejemplo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { imagenUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text("Pizza")
            }
            Button(
                onClick = { imagenUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text("Hamburguesa")
            }
            Button(
                onClick = { imagenUrl = "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=400" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text("Postre")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val tiempoInt = tiempo.toIntOrNull() ?: 0
                if (nombre.isNotEmpty() &&
                    descripcion.isNotEmpty() &&
                    tiempoInt > 0 &&
                    categoria.isNotEmpty() &&
                    imagenUrl.isNotEmpty()) {
                    onAgregar(
                        nombre,
                        descripcion,
                        tiempoInt,
                        categoria,
                        imagenUrl.trim()
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = nombre.isNotEmpty() &&
                    descripcion.isNotEmpty() &&
                    tiempo.toIntOrNull() ?: 0 > 0 &&
                    categoria.isNotEmpty() &&
                    imagenUrl.isNotEmpty()
        ) {
            Text("Agregar Receta", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaPrincipal() {
    FastyRecipesTheme {
        // Para el preview necesitarías un ViewModel mock
    }
}