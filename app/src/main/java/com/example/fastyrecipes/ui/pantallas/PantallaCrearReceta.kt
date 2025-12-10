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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.fastyrecipes.data.ImageUrls
import com.example.fastyrecipes.modelo.Ingrediente
import com.example.fastyrecipes.ui.theme.FastyRecipesTheme
import com.example.fastyrecipes.viewmodels.RecetasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearReceta(
    viewModel: RecetasViewModel,
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
    val textosTraducidos by viewModel.textosTraducidos.collectAsStateWithLifecycle()

    fun texto(key: String): String {
        return textosTraducidos[key] ?: key
    }

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
                            1 -> "${texto("paso")} 1: ${texto("informacion_basica")}"
                            2 -> "${texto("paso")} 2: ${texto("ingredientes")}"
                            3 -> "${texto("paso")} 3: ${texto("pasos")}"
                            4 -> "${texto("paso")} 4: ${texto("categoria_imagen")}"
                            else -> texto("crear_receta")
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
                        Icon(Icons.Default.ArrowBack, contentDescription = texto("volver"))
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
                            Text(texto("guardar"))
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
                            Text(texto("siguiente"))
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
                    textosTraducidos = textosTraducidos,
                    nombre = nombre,
                    tiempo = tiempo,
                    onNombreChange = { nombre = it },
                    onTiempoChange = { tiempo = it }
                )

                2 -> PasoIngredientes(
                    textosTraducidos = textosTraducidos,
                    ingredientes = ingredientes,
                    nuevoNombre = nuevoIngredienteNombre,
                    nuevoCantidad = nuevoIngredienteCantidad,
                    onNombreChange = { nuevoIngredienteNombre = it },
                    onCantidadChange = { nuevoIngredienteCantidad = it },
                    onUnidadChange = { nuevoIngredienteUnidad = it },
                    onAddIngrediente = {
                        if (nuevoIngredienteNombre.isNotBlank() && nuevoIngredienteCantidad.isNotBlank()) {
                            val nuevoIngrediente = Ingrediente(
                                nombre = nuevoIngredienteNombre,
                                cantidad = nuevoIngredienteCantidad
                            )
                            ingredientes = ingredientes + nuevoIngrediente
                            nuevoIngredienteNombre = ""
                            nuevoIngredienteCantidad = ""
                        }
                    },
                    onRemoveIngrediente = { ingrediente ->
                        ingredientes = ingredientes - ingrediente
                    }
                )

                3 -> PasoPreparacion(
                    textosTraducidos = textosTraducidos,
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
                    textosTraducidos = textosTraducidos,
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
    textosTraducidos: Map<String, String>,
    nombre: String,
    tiempo: String,
    onNombreChange: (String) -> Unit,
    onTiempoChange: (String) -> Unit
) {
    fun texto(key: String): String {
        return textosTraducidos[key] ?: key
    }

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
                        texto("informacion_basica"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = onNombreChange,
                        label = { Text("${texto("nombre_receta")} *") },
                        placeholder = { Text(texto("ej_nombre_receta")) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nombre.isEmpty()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = tiempo,
                        onValueChange = onTiempoChange,
                        label = { Text("${texto("tiempo_preparacion")} *") },
                        placeholder = { Text(texto("ej_tiempo")) },
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
    textosTraducidos: Map<String, String>,
    ingredientes: List<Ingrediente>,
    nuevoNombre: String,
    nuevoCantidad: String,
    onNombreChange: (String) -> Unit,
    onCantidadChange: (String) -> Unit,
    onUnidadChange: (String) -> Unit,
    onAddIngrediente: () -> Unit,
    onRemoveIngrediente: (Ingrediente) -> Unit
) {
    fun texto(key: String): String {
        return textosTraducidos[key] ?: key
    }

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
                        texto("ingredientes"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Contador de ingredientes
                    Text(
                        "${texto("ingredientes_anadidos")}: ${ingredientes.size}",
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
                            label = { Text("${texto("nombre_ingrediente")} *") },
                            placeholder = { Text(texto("ej_ingrediente")) },
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
                                label = { Text("${texto("cantidad")} *") },
                                placeholder = { Text(texto("ej_cantidad")) },
                                modifier = Modifier.weight(1f)
                            )

                        }

                        Button(
                            onClick = onAddIngrediente,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = nuevoNombre.isNotBlank() && nuevoCantidad.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = texto("anadir"))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(texto("agregar_ingrediente"))
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
                                text = if (ingrediente.cantidad.isNotEmpty()) {
                                    "${ingrediente.cantidad} "
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
                                contentDescription = texto("eliminar_ingrediente"),
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
                            contentDescription = texto("sin_ingredientes"),
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            texto("anade_ingredientes_receta"),
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
    textosTraducidos: Map<String, String>,
    pasos: List<String>,
    nuevoPaso: String,
    onPasoChange: (String) -> Unit,
    onAddPaso: () -> Unit,
    onRemovePaso: (String) -> Unit
) {
    fun texto(key: String): String {
        return textosTraducidos[key] ?: key
    }

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
                        texto("pasos_preparacion"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Contador de pasos
                    Text(
                        "${texto("pasos_anadidos")}: ${pasos.size}",
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
                            label = { Text(texto("nuevo_paso")) },
                            placeholder = { Text(texto("ej_paso")) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onAddPaso,
                            enabled = nuevoPaso.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = texto("anadir_paso"))
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
                                contentDescription = texto("eliminar_paso"),
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
                            contentDescription = texto("sin_pasos"),
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            texto("anade_pasos_preparacion"),
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
    textosTraducidos: Map<String, String>,
    categoria: String,
    imagenUrl: String,
    onCategoriaChange: (String) -> Unit,
    onImagenUrlChange: (String) -> Unit,
    nombre: String,
    tiempo: String,
    ingredientesCount: Int,
    pasosCount: Int
) {
    fun texto(key: String): String {
        return textosTraducidos[key] ?: key
    }

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
                    texto("categoria_imagen"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = categoria,
                    onValueChange = onCategoriaChange,
                    label = { Text("${texto("categoria")} *") },
                    placeholder = { Text(texto("ej_categoria")) },
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
                                    contentDescription = texto("generar_nueva_imagen")
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = imagenUrl,
                    onValueChange = onImagenUrlChange,
                    label = { Text("${texto("imagen_url")} *") },
                    placeholder = { Text(texto("url_automatica")) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = imagenUrl.isEmpty(),
                    supportingText = {
                        Text(texto("url_generada_automaticamente"))
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
                        Text(texto("imagen_por_categoria"), fontSize = 12.sp)
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
                        Text(texto("imagen_aleatoria"), fontSize = 12.sp)
                    }
                }

                // Sugerencias de categorías populares
                Text(
                    text = texto("categorias_populares"),
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
                        text = texto("vista_previa_imagen"),
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
                            contentDescription = texto("vista_previa_imagen"),
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
                    texto("resumen"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text("${texto("nombre")}: $nombre", fontWeight = FontWeight.Medium)
                Text("${texto("tiempo")}: $tiempo ${texto("minutos")}")
                Text("${texto("ingredientes")}: $ingredientesCount")
                Text("${texto("pasos")}: $pasosCount")
                Text("${texto("categoria")}: $categoria")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaCrearReceta() {
    FastyRecipesTheme {
        // PantallaCrearReceta(
        //     viewModel = ...,
        //     onGuardar = { _, _, _, _, _, _ -> },
        //     onCancelar = {}
        // )
    }
}