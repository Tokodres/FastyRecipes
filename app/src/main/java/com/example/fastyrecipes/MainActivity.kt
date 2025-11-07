package com.example.fastyrecipes

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fastyrecipes.controller.FastyController
import com.example.fastyrecipes.modelo.Receta
import com.example.fastyrecipes.ui.theme.FastyRecipesTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val fastyController by lazy { FastyController(this) }
    private val viewModel by viewModels<RecetasViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RecetasViewModel(fastyController) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FastyRecipesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FastyRecipesApp(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

class RecetasViewModel(private val controller: FastyController) : ViewModel() {

    // Flow que observa cambios en tiempo real de la base de datos
    val recetas = controller.obtenerTodasLasRecetas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var isLoading by mutableStateOf(true)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            try {
                isLoading = true
                // Cargar datos iniciales en la base de datos
                controller.cargarDatosIniciales()
                error = null
            } catch (e: Exception) {
                error = "Error inicializando datos: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleFavorito(receta: Receta) {
        viewModelScope.launch {
            try {
                controller.marcarComoFavorita(receta.id, !receta.esFavorita)
            } catch (e: Exception) {
                error = "Error actualizando favorito: ${e.message}"
            }
        }
    }

    fun agregarReceta(nombre: String, descripcion: String, tiempo: Int, categoria: String) {
        viewModelScope.launch {
            try {
                val nuevaReceta = Receta(
                    nombre = nombre,
                    descripcion = descripcion,
                    tiempoPreparacion = tiempo,
                    categoria = categoria
                )
                controller.insertarReceta(nuevaReceta)
            } catch (e: Exception) {
                error = "Error agregando receta: ${e.message}"
            }
        }
    }

    fun recargarDatos() {
        viewModelScope.launch {
            try {
                isLoading = true
                controller.cargarDatosIniciales()
                error = null
            } catch (e: Exception) {
                error = "Error recargando datos: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun eliminarReceta(receta: Receta) {
        viewModelScope.launch {
            try {
                controller.eliminarReceta(receta)
                error = null
            } catch (e: Exception) {
                error = "Error eliminando receta: ${e.message}"
            }
        }
    }

    fun eliminarTodasLasRecetas() {
        viewModelScope.launch {
            try {
                // Obtener todas las recetas y eliminarlas una por una
                val todasRecetas = recetas.value
                todasRecetas.forEach { receta ->
                    controller.eliminarReceta(receta)
                }
                error = null
            } catch (e: Exception) {
                error = "Error eliminando todas las recetas: ${e.message}"
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastyRecipesApp(
    viewModel: RecetasViewModel,
    modifier: Modifier = Modifier
) {
    // Observar el Flow de recetas en tiempo real
    val recetas by viewModel.recetas.collectAsState()
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val error by remember { derivedStateOf { viewModel.error } }

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var recetaAEliminar by remember { mutableStateOf<Receta?>(null) }

    var nuevaRecetaNombre by remember { mutableStateOf("") }
    var nuevaRecetaDesc by remember { mutableStateOf("") }
    var nuevaRecetaTiempo by remember { mutableStateOf("") }
    var nuevaRecetaCategoria by remember { mutableStateOf("Cena") }

    // Dialog para confirmar eliminación de receta específica
    recetaAEliminar?.let { receta ->
        AlertDialog(
            onDismissRequest = { recetaAEliminar = null },
            title = { Text("Eliminar Receta") },
            text = {
                Text("¿Estás seguro de que quieres eliminar '${receta.nombre}'? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarReceta(receta)
                        recetaAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { recetaAEliminar = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog para confirmar eliminación de todas las recetas
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Eliminar Todas las Recetas") },
            text = {
                Text("¿Estás seguro de que quieres eliminar TODAS las recetas (${recetas.size})? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarTodasLasRecetas()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar Todas")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAllDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog para agregar nueva receta
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Agregar Nueva Receta") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nuevaRecetaNombre,
                        onValueChange = { nuevaRecetaNombre = it },
                        label = { Text("Nombre de la receta") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nuevaRecetaDesc,
                        onValueChange = { nuevaRecetaDesc = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nuevaRecetaTiempo,
                        onValueChange = { nuevaRecetaTiempo = it },
                        label = { Text("Tiempo (minutos)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nuevaRecetaCategoria,
                        onValueChange = { nuevaRecetaCategoria = it },
                        label = { Text("Categoría") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevaRecetaNombre.isNotBlank() && nuevaRecetaTiempo.isNotBlank()) {
                            viewModel.agregarReceta(
                                nuevaRecetaNombre,
                                nuevaRecetaDesc,
                                nuevaRecetaTiempo.toIntOrNull() ?: 15,
                                nuevaRecetaCategoria
                            )
                            // Limpiar campos
                            nuevaRecetaNombre = ""
                            nuevaRecetaDesc = ""
                            nuevaRecetaTiempo = ""
                            nuevaRecetaCategoria = "Cena"
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "🍳 Fasty Recipes",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Base de datos en tiempo real ✅",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de acción PRINCIPALES
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Agregar")
            }

            Button(
                onClick = { viewModel.recargarDatos() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Recargar")
            }
        }

        // Botones de ELIMINACIÓN (solo mostrar si hay recetas)
        if (recetas.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showDeleteAllDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar todas")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar Todas")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lista de recetas
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Conectando con base de datos...")
                    Text(
                        text = "📊",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        } else if (recetas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📝",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay recetas",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Usa el botón 'Agregar' para crear una",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                text = "Tus Recetas (${recetas.size}) - Tiempo Real ✅",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recetas) { receta ->
                    TarjetaReceta(
                        receta = receta,
                        onToggleFavorito = { viewModel.toggleFavorito(receta) },
                        onEliminar = { recetaAEliminar = receta }
                    )
                }
            }
        }

        // Footer informativo
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "💡 Click en ⋮ para opciones • Los cambios se guardan automáticamente",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaReceta(
    receta: Receta,
    onToggleFavorito: () -> Unit,
    onEliminar: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = {
            // Navegar a detalle de receta
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = receta.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Menú de opciones (tres puntos)
                Box {
                    IconButton(
                        onClick = { showMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            onClick = {
                                onEliminar()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = receta.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Información de la receta
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⏱️ ${receta.tiempoPreparacion} min",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "🍽️ ${receta.categoria}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "ID: ${receta.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Botón de favorito
                IconButton(
                    onClick = onToggleFavorito
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Favorita",
                        tint = if (receta.esFavorita) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                }
            }

            // Indicador de favorito
            if (receta.esFavorita) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⭐ Favorita",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Mantén tu Greeting original
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FastyRecipesTheme {
        Greeting("Android")
    }
}

@Preview(showBackground = true)
@Composable
fun FastyRecipesAppPreview() {
    val context = LocalContext.current
    FastyRecipesTheme {
        FastyRecipesApp(
            viewModel = RecetasViewModel(FastyController(context))
        )
    }
}