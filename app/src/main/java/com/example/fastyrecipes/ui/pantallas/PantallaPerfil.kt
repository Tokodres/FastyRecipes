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
    onCerrarSesion: () -> Unit,
    onCambiarIdioma: () -> Unit
) {

    val usuarioActual by viewModel.usuarioActual.collectAsStateWithLifecycle()
    val recetas by viewModel.recetas.collectAsStateWithLifecycle()
    val esInvitado by viewModel.esInvitado.collectAsStateWithLifecycle()
    val idiomaActual by viewModel.idiomaActual.collectAsStateWithLifecycle()
    val textosTraducidos by viewModel.textosTraducidos.collectAsStateWithLifecycle()

    // Función helper para obtener texto traducido
    fun texto(key: String): String {
        return textosTraducidos[key] ?: key
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        texto("mi_perfil"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = texto("volver"))
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
                            Icons.Default.Person,
                            contentDescription = "Invitado",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            texto("invitado_mensaje1"),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            texto("invitado_mensaje2"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onCerrarSesion
                        ) {
                            Text(texto("iniciar_sesion"))
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
                                            AsyncImage(
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
                                        text = "${texto("miembro_desde")}${formatearFecha(usuario.fechaRegistro)}",
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
                                        text = texto("mis_estadisticas"),
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
                                                text = texto("recetas"),
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
                                                text = texto("favoritas"),
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
                                                text = texto("busquedas"),
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
                                    // Título de la sección (sin acción)
                                    ListItem(
                                        headlineContent = { Text(texto("configuracion_cuenta")) },
                                        leadingContent = {
                                            Icon(
                                                Icons.Default.Settings,
                                                contentDescription = texto("configuracion")
                                            )
                                        }
                                    )

                                    Divider()

                                    // Opción: Cambiar idioma
                                    ListItem(
                                        headlineContent = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(texto("idioma"))
                                                Text(
                                                    text = idiomaActual,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        },
                                        leadingContent = {
                                            Icon(
                                                Icons.Default.Language,
                                                contentDescription = texto("idioma")
                                            )
                                        },
                                        modifier = Modifier.clickable { onCambiarIdioma() }
                                    )

                                    Divider()

                                    ListItem(
                                        headlineContent = { Text(texto("cambiar_contrasena")) },
                                        leadingContent = {
                                            Icon(
                                                Icons.Default.Lock,
                                                contentDescription = texto("contrasena")
                                            )
                                        },
                                        modifier = Modifier.clickable { /* Cambiar contraseña */ }
                                    )

                                    Divider()

                                    ListItem(
                                        headlineContent = { Text(texto("notificaciones")) },
                                        leadingContent = {
                                            Icon(
                                                Icons.Default.Notifications,
                                                contentDescription = texto("notificaciones")
                                            )
                                        },
                                        modifier = Modifier.clickable { /* Configurar notificaciones */ }
                                    )

                                    Divider()

                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                texto("cerrar_sesion"),
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                Icons.Default.Logout,
                                                contentDescription = texto("cerrar_sesion"),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        modifier = Modifier.clickable { onCerrarSesion() }
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
        //     onCerrarSesion = {},
        //     onCambiarIdioma = {}
        // )
    }
}