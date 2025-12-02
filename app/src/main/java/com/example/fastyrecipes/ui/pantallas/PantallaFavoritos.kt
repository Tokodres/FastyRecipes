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
fun PantallaFavoritos(
    viewModel: RecetasViewModel,
    onBack: () -> Unit,
    onNavigateToInicio: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFavoritos: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToDetalleReceta: (Receta) -> Unit  // NUEVO PARÁMETRO
) {

    val recetasFavoritas by viewModel.recetasFavoritas.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Tus Favoritos",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Mostrar contador de favoritos en la barra superior
                    if (recetasFavoritas.isNotEmpty()) {
                        Badge(
                            modifier = Modifier.padding(end = 16.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = recetasFavoritas.size.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentScreen = "favoritos",
                onNavigateToInicio = onNavigateToInicio,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToFavoritos = onNavigateToFavoritos,
                onNavigateToPerfil = onNavigateToPerfil
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Estado de carga
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Cargando tus favoritos...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Contenido principal
                if (recetasFavoritas.isEmpty()) {
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
                                contentDescription = "Sin favoritos",
                                modifier = Modifier.size(96.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "No tienes recetas favoritas",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Agrega recetas a favoritos para verlas aquí",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = onNavigateToSearch,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    "Explorar Recetas",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                } else {
                    // Contador y estadísticas de favoritos
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Tus recetas favoritas",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${recetasFavoritas.size} recetas guardadas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Lista de recetas favoritas
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(recetasFavoritas) { receta ->
                            RecetaItemFavoritos(
                                receta = receta,
                                onToggleFavorito = { viewModel.toggleFavorito(receta) },
                                onVerDetalle = { onNavigateToDetalleReceta(receta) }  // NUEVO
                            )
                        }
                    }
                }
            }
        }

        // Mostrar errores
        error?.let { errorMessage ->
            LaunchedEffect(errorMessage) {
                // Podrías mostrar un Snackbar aquí
            }
        }
    }
}

// Componente específico para favoritos - MODIFICADO
@Composable
fun RecetaItemFavoritos(
    receta: Receta,
    onToggleFavorito: () -> Unit,
    onVerDetalle: () -> Unit  // NUEVO PARÁMETRO
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVerDetalle() },  // HACE CLICKABLE TODA LA TARJETA
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            // IMAGEN DE LA RECETA - AHORA ES CLICKABLE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { onVerDetalle() }
            ) {
                RecetaImage(
                    imageUrl = receta.imagenUrl,
                    contentDescription = "Imagen de ${receta.nombre}",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
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
                            .clickable { onVerDetalle() }
                    )

                    // Botón de favorito (siempre activo en favoritos)
                    IconButton(
                        onClick = onToggleFavorito,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Quitar de favoritos",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Descripción
                Text(
                    text = receta.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth()
                        .clickable { onVerDetalle() },
                    maxLines = 3
                )

                // Información de tiempo y categoría
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Tiempo de preparación
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Tiempo de preparación",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${receta.tiempoPreparacion} min",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Categoría
                    AssistChip(
                        onClick = { /* Navegar a recetas por categoría */ },
                        label = {
                            Text(
                                text = receta.categoria,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
        }
    }
}