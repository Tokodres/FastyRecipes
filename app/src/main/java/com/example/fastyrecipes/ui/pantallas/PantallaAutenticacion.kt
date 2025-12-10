package com.example.fastyrecipes.ui.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fastyrecipes.R
import com.example.fastyrecipes.ui.theme.FastyRecipesTheme
import com.example.fastyrecipes.viewmodels.RecetasViewModel

@Composable
fun PantallaAutenticacion(
    viewModel: RecetasViewModel,
    onIniciarSesion: (String, String) -> Unit,
    onRegistrarse: (String, String, String) -> Unit,
    onIniciarComoInvitado: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val textosTraducidos by viewModel.textosTraducidos.collectAsStateWithLifecycle()

    fun texto(key: String): String {
        return textosTraducidos[key] ?: key
    }

    var esModoRegistro by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var confirmarContraseña by remember { mutableStateOf("") }
    var mostrarContraseña by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo con imagen
        // Overlay oscuro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo/Título
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = texto("app_name"),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = texto("app_slogan"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Card con formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (esModoRegistro) texto("registrarse_titulo") else texto("iniciar_sesion_titulo"),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Mostrar error si existe
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (esModoRegistro) {
                        // Campo Nombre (solo en registro)
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text(texto("nombre_usuario")) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = texto("nombre_usuario"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Campo Correo
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text(texto("correo_electronico")) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = texto("correo_electronico"))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Campo Contraseña
                    OutlinedTextField(
                        value = contraseña,
                        onValueChange = { contraseña = it },
                        label = { Text(texto("contraseña")) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = texto("contraseña"))
                        },
                        trailingIcon = {
                            IconButton(onClick = { mostrarContraseña = !mostrarContraseña }) {
                                Icon(
                                    if (mostrarContraseña) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = texto("mostrar_contraseña")
                                )
                            }
                        },
                        visualTransformation = if (mostrarContraseña) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (esModoRegistro) {
                        // Campo Confirmar Contraseña
                        OutlinedTextField(
                            value = confirmarContraseña,
                            onValueChange = { confirmarContraseña = it },
                            label = { Text(texto("confirmar_contraseña")) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = texto("confirmar_contraseña"))
                            },
                            trailingIcon = {
                                IconButton(onClick = { mostrarContraseña = !mostrarContraseña }) {
                                    Icon(
                                        if (mostrarContraseña) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = texto("mostrar_contraseña")
                                    )
                                }
                            },
                            visualTransformation = if (mostrarContraseña) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            isError = confirmarContraseña.isNotEmpty() && contraseña != confirmarContraseña
                        )
                    }

                    // Botón principal
                    Button(
                        onClick = {
                            if (esModoRegistro) {
                                if (contraseña == confirmarContraseña) {
                                    onRegistrarse(nombre, correo, contraseña)
                                }
                            } else {
                                onIniciarSesion(correo, contraseña)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading && correo.isNotEmpty() && contraseña.isNotEmpty() &&
                                (!esModoRegistro || (nombre.isNotEmpty() && contraseña == confirmarContraseña))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (esModoRegistro) texto("registrarse_titulo") else texto("iniciar_sesion_titulo"),
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Botón para cambiar entre registro/inicio de sesión
                    TextButton(
                        onClick = {
                            esModoRegistro = !esModoRegistro
                            // Limpiar campos
                            if (esModoRegistro) {
                                nombre = ""
                                confirmarContraseña = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (esModoRegistro) texto("ya_tienes_cuenta") else texto("no_tienes_cuenta"),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Botón para continuar como invitado
                    OutlinedButton(
                        onClick = onIniciarComoInvitado,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PersonOutline, contentDescription = texto("iniciar_como_invitado"))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(texto("iniciar_como_invitado"))
                    }
                }
            }

            // Información adicional
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = texto("terminos_condiciones"),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaAutenticacion() {
    FastyRecipesTheme {
        // PantallaAutenticacion(
        //     viewModel = ...,
        //     onIniciarSesion = { _, _ -> },
        //     onRegistrarse = { _, _, _ -> },
        //     onIniciarComoInvitado = {},
        //     isLoading = false
        // )
    }
}