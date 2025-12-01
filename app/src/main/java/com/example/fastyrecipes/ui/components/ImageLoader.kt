package com.example.fastyrecipes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import androidx.compose.material3.*

@Composable
fun RecetaImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String = "Imagen de receta"
) {
    // DEBUG DETALLADO
    LaunchedEffect(imageUrl) {
        println("🎯 DEBUG RecetaImage - URL recibida:")
        println("   - Valor: '$imageUrl'")
        println("   - Es null: ${imageUrl == null}")
        println("   - Es empty: ${imageUrl?.isEmpty() ?: true}")
        println("   - Longitud: ${imageUrl?.length ?: 0}")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                when {
                    imageUrl == null -> Color.Red
                    imageUrl.isEmpty() -> Color.Yellow
                    else -> Color(0xFF4CAF50) // Verde
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            imageUrl == null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("❌ URL ES NULL", color = Color.White, textAlign = TextAlign.Center)
                    Text("Firestore devolvió null", color = Color.White, textAlign = TextAlign.Center)
                    Text("Verifica ViewModel/Firestore", color = Color.White, textAlign = TextAlign.Center)
                }
            }
            imageUrl.isEmpty() -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️ URL VACÍA", color = Color.Black, textAlign = TextAlign.Center)
                    Text("Firestore: ''", color = Color.Black, textAlign = TextAlign.Center)
                    Text("URL se guardó vacía", color = Color.Black, textAlign = TextAlign.Center)
                }
            }
            else -> {
                // INTENTAR CARGAR LA IMAGEN CON SUBCOMPOSEASYNCIMAGE
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth(),
                    loading = {
                        // Mientras carga
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "Cargando imagen...",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "URL: ${imageUrl.take(25)}...",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    error = {
                        // Si hay error en la carga
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "❌ ERROR COIL",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "No se pudo cargar la imagen",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "URL: ${imageUrl.take(20)}...",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Verifica internet/URL",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    },
                    success = {
                        // Debug cuando carga exitosamente
                        println("✅ DEBUG: Imagen cargada EXITOSAMENTE desde: $imageUrl")
                    }
                )
            }
        }
    }
}