package com.example.fastyrecipes.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.fastyrecipes.ui.components.RecetaImage
import com.example.fastyrecipes.ui.theme.FastyRecipesTheme
import com.example.fastyrecipes.viewmodels.RecetasViewModel
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(
    viewModel: RecetasViewModel,
    onNavigateToInicio: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFavoritos: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToCrearReceta: () -> Unit,
    onNavigateToDetalleReceta: (Receta) -> Unit  // NUEVO PARÁMETRO
) {

    val recetas by viewModel.recetas.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

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
                onClick = onNavigateToCrearReceta,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear receta")
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
                                onEliminar = { viewModel.eliminarReceta(receta) },
                                onVerDetalle = { onNavigateToDetalleReceta(receta) }  // NUEVO
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
    }
}

// Componente original de RecetaItem (con eliminar y favoritos) - MODIFICADO
@Composable
fun RecetaItemOriginal(
    receta: Receta,
    onToggleFavorito: () -> Unit,
    onEliminar: () -> Unit,
    onVerDetalle: () -> Unit  // NUEVO PARÁMETRO
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onVerDetalle() },  // HACE CLICKABLE TODA LA TARJETA
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
            // Imagen de la receta - AHORA ES CLICKABLE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { onVerDetalle() }  // TAMBIÉN LA IMAGEN ES CLICKABLE
            ) {
                RecetaImage(
                    imageUrl = receta.imagenUrl,
                    contentDescription = "Imagen de ${receta.nombre}",
                    modifier = Modifier.fillMaxSize()
                )
            }

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
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onVerDetalle() }  // EL NOMBRE TAMBIÉN ES CLICKABLE
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
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable { onVerDetalle() }  // LA DESCRIPCIÓN TAMBIÉN ES CLICKABLE
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