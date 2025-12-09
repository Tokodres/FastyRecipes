package com.example.fastyrecipes.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import coil.compose.AsyncImage
import com.example.fastyrecipes.modelo.Usuario
import com.example.fastyrecipes.ui.components.BottomNavigationBar
import com.example.fastyrecipes.ui.theme.FastyRecipesTheme
import com.example.fastyrecipes.viewmodels.RecetasViewModel
import androidx.compose.foundation.clickable
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(
    viewModel: RecetasViewModel,
    onBack: () -> Unit,
    onNavigateToInicio: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFavoritos: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {

    val usuarioActual by viewModel.usuarioActual.collectAsStateWithLifecycle()
    val recetas by viewModel.recetas.collectAsStateWithLifecycle()
    val esInvitado by viewModel.esInvitado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Mi Perfil",
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
            if (esInvitado) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Person, // CAMBIADO: PersonOutline no existe en Icons.Default
                            contentDescription = "Invitado",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Estás usando la aplicación como invitado",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Inicia sesión para guardar tus recetas y preferencias",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onCerrarSesion
                        ) {
                            Text("Iniciar Sesión")
                        }
                    }
                }
            } else {
                usuarioActual?.let { usuario ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (usuario.fotoPerfil.isNotEmpty()) {
                                            AsyncImage( // CORREGIDO: Importado correctamente
                                                model = usuario.fotoPerfil,
                                                contentDescription = "Foto de perfil",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = "Foto de perfil",
                                                modifier = Modifier.size(50.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = usuario.nombre,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = usuario.correo,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Miembro desde: ${formatearFecha(usuario.fechaRegistro)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Mis Estadísticas",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = recetas.size.toString(),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Recetas",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val favoritasCount = recetas.count { receta ->
                                                usuario.recetasGuardadas.contains(receta.id)
                                            }
                                            Text(
                                                text = favoritasCount.toString(),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Favoritas",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = usuario.historialBusquedas.size.toString(),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Búsquedas",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    ListItem(
                                        headlineContent = { Text("Configuración de la cuenta") },
                                        leadingContent = {
                                            Icon(
                                                Icons.Default.Settings, // CAMBIADO
                                                contentDescription = "Configuración"
                                            )
                                        }
                                    )

                                    Divider()

                                    ListItem(
                                        headlineContent = { Text("Cambiar contraseña") },
                                        leadingContent = {
                                            Icon(
                                                Icons.Default.Lock, // CAMBIADO
                                                contentDescription = "Contraseña"
                                            )
                                        },
                                        modifier = Modifier.clickable { /* Cambiar contraseña */ } // CORREGIDO
                                    )

                                    Divider()

                                    ListItem(
                                        headlineContent = { Text("Notificaciones") },
                                        leadingContent = {
                                            Icon(
                                                Icons.Default.Notifications, // CAMBIADO
                                                contentDescription = "Notificaciones"
                                            )
                                        },
                                        modifier = Modifier.clickable { /* Configurar notificaciones */ } // CORREGIDO
                                    )

                                    Divider()

                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                "Cerrar Sesión",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                Icons.Default.Logout, // CAMBIADO
                                                contentDescription = "Cerrar sesión",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        modifier = Modifier.clickable { onCerrarSesion() } // CORREGIDO
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Función auxiliar para formatear fecha
private fun formatearFecha(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(date)
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaPerfil() {
    FastyRecipesTheme {
        // PantallaPerfil(
        //     viewModel = ...,
        //     onBack = {},
        //     onNavigateToInicio = {},
        //     onNavigateToSearch = {},
        //     onNavigateToFavoritos = {},
        //     onNavigateToPerfil = {},
        //     onCerrarSesion = {}
        // )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUsuarioCard() {
    FastyRecipesTheme {
        val usuarioEjemplo = Usuario(
            id = "1",
            nombre = "Chef María",
            correo = "maria@cocina.com",
            recetasGuardadas = listOf("receta1", "receta2"), // CORREGIDO
            historialBusquedas = listOf("pollo", "postres") // CORREGIDO
        )

        // UsuarioCard ya no existe en este archivo
        // Comentar o eliminar esta llamada
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUsuarioCardInactivo() {
    FastyRecipesTheme {
        val usuarioEjemplo = Usuario(
            id = "1",
            nombre = "Chef María",
            correo = "maria@cocina.com",
            recetasGuardadas = listOf("receta1", "receta2"), // CORREGIDO
            historialBusquedas = listOf("pollo", "postres") // CORREGIDO
        )

        // UsuarioCard ya no existe en este archivo
        // Comentar o eliminar esta llamada
    }
}