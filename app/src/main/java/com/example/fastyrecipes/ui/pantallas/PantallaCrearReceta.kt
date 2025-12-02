package com.example.fastyrecipes.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fastyrecipes.data.ImageUrls
import com.example.fastyrecipes.modelo.Ingrediente
import com.example.fastyrecipes.ui.theme.FastyRecipesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearReceta(
    onGuardar: (
        nombre: String,
        tiempo: Int,
        ingredientes: List<Ingrediente>,
        pasos: List<String>,
        categoria: String,
        imagenUrl: String
    ) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var tiempo by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var imagenUrl by remember { mutableStateOf("") }

    // Estados para ingredientes
    var nuevoIngredienteNombre by remember { mutableStateOf("") }
    var nuevoIngredienteCantidad by remember { mutableStateOf("") }
    var nuevoIngredienteUnidad by remember { mutableStateOf("") }
    var ingredientes by remember { mutableStateOf(listOf<Ingrediente>()) }

    // Estados para pasos
    var nuevoPaso by remember { mutableStateOf("") }
    var pasos by remember { mutableStateOf(listOf<String>()) }

    var step by remember { mutableStateOf(1) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        when (step) {
                            1 -> "Paso 1: Información Básica"
                            2 -> "Paso 2: Ingredientes"
                            3 -> "Paso 3: Pasos de Preparación"
                            4 -> "Paso 4: Categoría e Imagen"
                            else -> "Crear Receta"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step > 1) {
                            step--
                        } else {
                            onCancelar()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (step == 4) {
                        Button(
                            onClick = {
                                onGuardar(
                                    nombre,
                                    tiempo.toIntOrNull() ?: 0,
                                    ingredientes,
                                    pasos,
                                    categoria,
                                    imagenUrl
                                )
                            },
                            enabled = nombre.isNotEmpty() &&
                                    tiempo.toIntOrNull() ?: 0 > 0 &&
                                    ingredientes.isNotEmpty() &&
                                    pasos.isNotEmpty() &&
                                    categoria.isNotEmpty() &&
                                    imagenUrl.isNotEmpty()
                        ) {
                            Text("Guardar")
                        }
                    } else {
                        Button(
                            onClick = { step++ },
                            enabled = when (step) {
                                1 -> nombre.isNotEmpty() && tiempo.toIntOrNull() ?: 0 > 0
                                2 -> ingredientes.isNotEmpty()
                                3 -> pasos.isNotEmpty()
                                else -> false
                            }
                        ) {
                            Text("Siguiente")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (step) {
                1 -> PasoInformacionBasica(
                    nombre = nombre,
                    tiempo = tiempo,
                    onNombreChange = { nombre = it },
                    onTiempoChange = { tiempo = it }
                )

                2 -> PasoIngredientes(
                    ingredientes = ingredientes,
                    nuevoNombre = nuevoIngredienteNombre,
                    nuevoCantidad = nuevoIngredienteCantidad,
                    nuevoUnidad = nuevoIngredienteUnidad,
                    onNombreChange = { nuevoIngredienteNombre = it },
                    onCantidadChange = { nuevoIngredienteCantidad = it },
                    onUnidadChange = { nuevoIngredienteUnidad = it },
                    onAddIngrediente = {
                        if (nuevoIngredienteNombre.isNotBlank() && nuevoIngredienteCantidad.isNotBlank()) {
                            val nuevoIngrediente = Ingrediente(
                                nombre = nuevoIngredienteNombre,
                                cantidad = nuevoIngredienteCantidad,
                                unidad = nuevoIngredienteUnidad
                            )
                            ingredientes = ingredientes + nuevoIngrediente
                            nuevoIngredienteNombre = ""
                            nuevoIngredienteCantidad = ""
                            nuevoIngredienteUnidad = ""
                        }
                    },
                    onRemoveIngrediente = { ingrediente ->
                        ingredientes = ingredientes - ingrediente
                    }
                )

                3 -> PasoPreparacion(
                    pasos = pasos,
                    nuevoPaso = nuevoPaso,
                    onPasoChange = { nuevoPaso = it },
                    onAddPaso = {
                        if (nuevoPaso.isNotBlank()) {
                            pasos = pasos + nuevoPaso
                            nuevoPaso = ""
                        }
                    },
                    onRemovePaso = { paso ->
                        pasos = pasos - paso
                    }
                )

                4 -> PasoFinal(
                    categoria = categoria,
                    imagenUrl = imagenUrl,
                    onCategoriaChange = {
                        categoria = it
                        // Generar imagen automáticamente cuando se escribe una categoría
                        if (it.isNotBlank() && imagenUrl.isEmpty()) {
                            imagenUrl = ImageUrls.getImageByCategory(it)
                        }
                    },
                    onImagenUrlChange = { imagenUrl = it },
                    nombre = nombre,
                    tiempo = tiempo,
                    ingredientesCount = ingredientes.size,
                    pasosCount = pasos.size
                )
            }
        }
    }
}

@Composable
fun PasoInformacionBasica(
    nombre: String,
    tiempo: String,
    onNombreChange: (String) -> Unit,
    onTiempoChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Información Básica",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = onNombreChange,
                        label = { Text("Nombre de la receta *") },
                        placeholder = { Text("Ej: Pollo al horno con patatas") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nombre.isEmpty()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = tiempo,
                        onValueChange = onTiempoChange,
                        label = { Text("Tiempo de preparación (minutos) *") },
                        placeholder = { Text("Ej: 60") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = tiempo.isEmpty() || tiempo.toIntOrNull() ?: 0 <= 0
                    )
                }
            }
        }
    }
}

@Composable
fun PasoIngredientes(
    ingredientes: List<Ingrediente>,
    nuevoNombre: String,
    nuevoCantidad: String,
    nuevoUnidad: String,
    onNombreChange: (String) -> Unit,
    onCantidadChange: (String) -> Unit,
    onUnidadChange: (String) -> Unit,
    onAddIngrediente: () -> Unit,
    onRemoveIngrediente: (Ingrediente) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Ingredientes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Contador de ingredientes
                    Text(
                        "Ingredientes añadidos: ${ingredientes.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Campos para añadir nuevo ingrediente
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Nombre del ingrediente
                        OutlinedTextField(
                            value = nuevoNombre,
                            onValueChange = onNombreChange,
                            label = { Text("Nombre del ingrediente *") },
                            placeholder = { Text("Ej: Harina de trigo") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Cantidad y unidad en una fila
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = nuevoCantidad,
                                onValueChange = onCantidadChange,
                                label = { Text("Cantidad *") },
                                placeholder = { Text("Ej: 200") },
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = nuevoUnidad,
                                onValueChange = onUnidadChange,
                                label = { Text("Unidad") },
                                placeholder = { Text("Ej: gramos, tazas") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = onAddIngrediente,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = nuevoNombre.isNotBlank() && nuevoCantidad.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Añadir Ingrediente")
                        }
                    }
                }
            }
        }

        // Lista de ingredientes
        if (ingredientes.isNotEmpty()) {
            items(ingredientes) { ingrediente ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = ingrediente.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (ingrediente.unidad.isNotEmpty()) {
                                    "${ingrediente.cantidad} ${ingrediente.unidad}"
                                } else {
                                    ingrediente.cantidad
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { onRemoveIngrediente(ingrediente) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar ingrediente",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "Sin ingredientes",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Añade los ingredientes de tu receta",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PasoPreparacion(
    pasos: List<String>,
    nuevoPaso: String,
    onPasoChange: (String) -> Unit,
    onAddPaso: () -> Unit,
    onRemovePaso: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Pasos de Preparación",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Contador de pasos
                    Text(
                        "Pasos añadidos: ${pasos.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Campo para añadir nuevo paso
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = nuevoPaso,
                            onValueChange = onPasoChange,
                            label = { Text("Nuevo paso") },
                            placeholder = { Text("Ej: Precalentar el horno a 180°C") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onAddPaso,
                            enabled = nuevoPaso.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir paso")
                        }
                    }
                }
            }
        }

        // Lista de pasos - FORMA CORREGIDA
        if (pasos.isNotEmpty()) {
            itemsIndexed(pasos) { index, paso ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Número del paso
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.shapes.small
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Texto del paso
                        Text(
                            text = paso,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Botón eliminar
                        IconButton(
                            onClick = { onRemovePaso(paso) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar paso",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "Sin pasos",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Añade los pasos de preparación",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PasoFinal(
    categoria: String,
    imagenUrl: String,
    onCategoriaChange: (String) -> Unit,
    onImagenUrlChange: (String) -> Unit,
    nombre: String,
    tiempo: String,
    ingredientesCount: Int,
    pasosCount: Int
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Categoría e Imagen",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = categoria,
                    onValueChange = onCategoriaChange,
                    label = { Text("Categoría *") },
                    placeholder = { Text("Ej: Cena, Postre, Ensalada, Pizza") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = categoria.isEmpty(),
                    trailingIcon = {
                        if (categoria.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    // Generar una nueva imagen para esta categoría
                                    onImagenUrlChange(ImageUrls.getImageByCategory(categoria))
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

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = imagenUrl,
                    onValueChange = onImagenUrlChange,
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
                                onImagenUrlChange(ImageUrls.getImageByCategory(categoria))
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
                            onImagenUrlChange(ImageUrls.getRandomImageUrl())
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

                // Sugerencias de categorías populares
                Text(
                    text = "Categorías populares:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Categorías en dos filas simples
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
                                onCategoriaChange(cat)
                                // Generar imagen para esta categoría
                                onImagenUrlChange(ImageUrls.getImageByCategory(cat))
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
                                onCategoriaChange(cat)
                                // Generar imagen para esta categoría
                                onImagenUrlChange(ImageUrls.getImageByCategory(cat))
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
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(vertical = 8.dp)
                            .clip(MaterialTheme.shapes.medium)
                    ) {
                        AsyncImage(
                            model = imagenUrl,
                            contentDescription = "Vista previa de imagen",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Resumen de la receta
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Resumen",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text("Nombre: $nombre", fontWeight = FontWeight.Medium)
                Text("Tiempo: $tiempo minutos")
                Text("Ingredientes: $ingredientesCount")
                Text("Pasos: $pasosCount")
                Text("Categoría: $categoria")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaCrearReceta() {
    FastyRecipesTheme {
        PantallaCrearReceta(
            onGuardar = { _, _, _, _, _, _ -> },
            onCancelar = {}
        )
    }
}