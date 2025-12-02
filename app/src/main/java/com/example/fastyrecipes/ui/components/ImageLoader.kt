package com.example.fastyrecipes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage

@Composable
fun RecetaImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String = "Imagen de receta"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Verificar si la URL está vacía
        if (imageUrl.isEmpty()) {
            // Mostrar placeholder si no hay URL
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Imagen no disponible",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF666666)
                    )
                    Text(
                        text = "Imagen no disponible",
                        color = Color(0xFF666666),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            // Cargar imagen desde URL
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    // Estado de carga
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                },
                error = {
                    // Estado de error
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF8BBD0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Error cargando imagen",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            )
        }
    }
}

// Versión simplificada para pantallas pequeñas
@Composable
fun SmallRecetaImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String = "Imagen de receta"
) {
    Box(
        modifier = modifier
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "Sin imagen",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF666666)
                )
            }
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}