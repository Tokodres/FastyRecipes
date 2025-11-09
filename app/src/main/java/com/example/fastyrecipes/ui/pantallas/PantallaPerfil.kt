package com.example.fastyrecipes.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fastyrecipes.modelo.Usuario
import com.example.fastyrecipes.ui.components.BottomNavigationBar
import com.example.fastyrecipes.ui.theme.FastyRecipesTheme
import com.example.fastyrecipes.viewmodels.RecetasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    viewModel: RecetasViewModel,
    onBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFavoritos: () -> Unit,
    onNavigateToPerfil: () -> Unit
) {

    val perfilesUsuarios by viewModel.perfilesUsuarios.collectAsStateWithLifecycle()
    val perfilActivo by viewModel.perfilActivo.collectAsStateWithLifecycle()

    var showCrearPerfilDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Perfiles de Usuario",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentScreen = "perfil",
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToFavoritos = onNavigateToFavoritos,
                onNavigateToPerfil = onNavigateToPerfil
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCrearPerfilDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear nuevo perfil")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Perfil activo actual
            if (perfilActivo != null) {
                Text(
                    text = "Perfil Activo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                UsuarioCard(
                    usuario = perfilActivo!!,
                    esActivo = true,
                    onSeleccionar = { },
                    onEliminar = { }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Lista de todos los perfiles
            Text(
                text = "Todos los Perfiles",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Text(
                text = "Selecciona un perfil para usar la app",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (perfilesUsuarios.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Sin perfiles",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay perfiles creados",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Crea tu primer perfil para comenzar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(perfilesUsuarios) { usuario ->
                        UsuarioCard(
                            usuario = usuario,
                            esActivo = usuario.id == perfilActivo?.id,
                            onSeleccionar = { viewModel.seleccionarPerfil(usuario) },
                            onEliminar = { viewModel.eliminarPerfil(usuario) }
                        )
                    }
                }
            }
        }

        // Dialog para crear nuevo perfil
        if (showCrearPerfilDialog) {
            DialogCrearPerfil(
                onConfirmar = { nombre, correo ->
                    viewModel.crearNuevoPerfil(nombre, correo)
                    showCrearPerfilDialog = false
                },
                onCancelar = { showCrearPerfilDialog = false }
            )
        }
    }
}

@Composable
fun UsuarioCard(
    usuario: Usuario,
    esActivo: Boolean,
    onSeleccionar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (esActivo) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del usuario
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del usuario
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = usuario.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = usuario.correo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Estadísticas del usuario
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (usuario.recetasGuardadas.isEmpty()) {
                            "📖 Sin recetas"
                        } else {
                            "📖 ${usuario.recetasGuardadas.size} recetas"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (usuario.historialBusquedas.isEmpty()) {
                            "🔍 Sin búsquedas"
                        } else {
                            "🔍 ${usuario.historialBusquedas.size} búsquedas"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (esActivo) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✓ Perfil activo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Botones de acción
            if (!esActivo) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Botón seleccionar compacto
                    TextButton(
                        onClick = onSeleccionar,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Usar", style = MaterialTheme.typography.labelMedium)
                    }

                    // Botón eliminar compacto
                    TextButton(
                        onClick = onEliminar,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Eliminar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogCrearPerfil(
    onConfirmar: (String, String) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Crear Nuevo Perfil") },
        text = {
            Column {
                Text(
                    text = "Crea un nuevo perfil para usar la aplicación",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del perfil") },
                    placeholder = { Text("Ej: Chef María") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo electrónico") },
                    placeholder = { Text("Ej: maria@cocina.com") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmar(nombre, correo) },
                enabled = nombre.isNotEmpty() && correo.isNotEmpty()
            ) {
                Text("Crear Perfil")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaPerfil() {
    FastyRecipesTheme {
        // PantallaPerfil(
        //     viewModel = ...,
        //     onBack = {},
        //     onNavigateToSearch = {},
        //     onNavigateToFavoritos = {},
        //     onNavigateToPerfil = {}
        // )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUsuarioCard() {
    FastyRecipesTheme {
        val usuarioEjemplo = Usuario(
            id = 1,
            nombre = "Chef María",
            correo = "maria@cocina.com",
            contraseña = "",
            recetasGuardadas = emptyList(),
            historialBusquedas = listOf("pollo", "postres")
        )

        UsuarioCard(
            usuario = usuarioEjemplo,
            esActivo = true,
            onSeleccionar = { },
            onEliminar = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUsuarioCardInactivo() {
    FastyRecipesTheme {
        val usuarioEjemplo = Usuario(
            id = 1,
            nombre = "Chef María",
            correo = "maria@cocina.com",
            contraseña = "",
            recetasGuardadas = emptyList(),
            historialBusquedas = listOf("pollo", "postres")
        )

        UsuarioCard(
            usuario = usuarioEjemplo,
            esActivo = false,
            onSeleccionar = { },
            onEliminar = { }
        )
    }
}