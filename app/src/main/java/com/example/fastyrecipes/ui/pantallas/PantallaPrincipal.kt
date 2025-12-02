package com.example.fastyrecipes.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fastyrecipes.data.ImageUrls
import com.example.fastyrecipes.modelo.Receta
import com.example.fastyrecipes.ui.components.BottomNavigationBar
import com.example.fastyrecipes.ui.components.RecetaImage
import com.example.fastyrecipes.ui.theme.FastyRecipesTheme
import com.example.fastyrecipes.viewmodels.RecetasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(
    viewModel: RecetasViewModel,
    onNavigateToInicio: () -> Unit,
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
                navigationIcon = {
                    // No necesitamos navigationIcon aquí, pero lo dejamos vacío
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

        // Dialog para agregar receta CON SCROLL
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Text(
                        "Agregar Nueva Receta",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                },
                text = {
                    // Agregar un ScrollView al contenido del diálogo
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp) // Altura máxima
                            .verticalScroll(rememberScrollState()) // Permite scroll
                    ) {
                        AgregarRecetaForm { nombre, descripcion, tiempo, categoria, imagenUrl ->
                            viewModel.agregarReceta(nombre, descripcion, tiempo, categoria, imagenUrl)
                            showAddDialog = false
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancelar")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.95f) // No ocupar todo el ancho
                    .heightIn(max = 600.dp) // Altura máxima del diálogo completo
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            RecetaImage(
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

@OptIn(ExperimentalMaterial3Api::class)
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
            onValueChange = {
                categoria = it
                // Si el usuario está escribiendo una categoría, sugerir una imagen automáticamente
                if (it.isNotBlank() && imagenUrl.isEmpty()) {
                    imagenUrl = ImageUrls.getImageByCategory(it)
                }
            },
            label = { Text("Categoría *") },
            placeholder = { Text("Ej: Cena, Postre, Ensalada, Pizza") },
            modifier = Modifier.fillMaxWidth(),
            isError = categoria.isEmpty(),
            trailingIcon = {
                if (categoria.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            // Generar una nueva imagen para esta categoría
                            imagenUrl = ImageUrls.getImageByCategory(categoria)
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Generar nueva imagen para esta categoría"
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = imagenUrl,
            onValueChange = { imagenUrl = it },
            label = { Text("URL de la imagen *") },
            placeholder = { Text("Se generará automáticamente basado en la categoría") },
            modifier = Modifier.fillMaxWidth(),
            isError = imagenUrl.isEmpty(),
            supportingText = {
                Text("La URL se genera automáticamente basada en la categoría")
            }
        )

        // Botones de acción para imágenes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botón para generar imagen basada en categoría
            Button(
                onClick = {
                    if (categoria.isNotEmpty()) {
                        imagenUrl = ImageUrls.getImageByCategory(categoria)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = categoria.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Imagen por categoría", fontSize = 12.sp)
            }

            // Botón para imagen aleatoria
            Button(
                onClick = {
                    imagenUrl = ImageUrls.getRandomImageUrl()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Imagen aleatoria", fontSize = 12.sp)
            }
        }

        // Sugerencias de categorías populares - SIMPLIFICADO sin FlowRow
        Text(
            text = "Categorías populares:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Categorías en dos filas simples sin FlowRow
        val categoriasPopulares = listOf("Pizza", "Hamburguesa", "Pasta", "Pollo", "Postre", "Ensalada")

        // Primera fila
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoriasPopulares.take(3).forEach { cat ->
                SuggestionChip(
                    onClick = {
                        categoria = cat
                        imagenUrl = ImageUrls.getImageByCategory(cat)
                    },
                    label = { Text(cat) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        // Segunda fila
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoriasPopulares.drop(3).forEach { cat ->
                SuggestionChip(
                    onClick = {
                        categoria = cat
                        imagenUrl = ImageUrls.getImageByCategory(cat)
                    },
                    label = { Text(cat) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        // Muestra la imagen actual como vista previa
        if (imagenUrl.isNotEmpty()) {
            Text(
                text = "Vista previa de la imagen:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = imagenUrl,
                    contentDescription = "Vista previa de imagen",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
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
        // PantallaPrincipal(
        //     viewModel = mockViewModel,
        //     onNavigateToInicio = {},
        //     onNavigateToSearch = {},
        //     onNavigateToFavoritos = {},
        //     onNavigateToPerfil = {}
        // )
    }
}