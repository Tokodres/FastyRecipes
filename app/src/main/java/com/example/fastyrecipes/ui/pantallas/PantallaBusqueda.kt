package com.example.fastyrecipes.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fastyrecipes.ui.components.RecetaCard
import com.example.fastyrecipes.ui.components.SearchBar
import com.example.fastyrecipes.ui.theme.FastyRecipesTheme
import com.example.fastyrecipes.viewmodels.RecetasViewModel
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBusqueda(
    viewModel: RecetasViewModel,
    onBack: () -> Unit,
    onNavigateToDetalleReceta: (com.example.fastyrecipes.modelo.Receta) -> Unit
) {

    // Estados del ViewModel
    val recetas by viewModel.recetasFiltradas.collectAsStateWithLifecycle()
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val categorias by viewModel.categoriasUnicas.collectAsStateWithLifecycle()

    // Obtener textos traducidos
    val textosTraducidos by viewModel.textosTraducidos.collectAsStateWithLifecycle()

    fun texto(key: String): String {
        return textosTraducidos[key] ?: key
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        texto("buscar_recetas"), // CAMBIADO: Usar traducción
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = texto("volver")) // CAMBIADO: Usar traducción
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Barra de búsqueda prominente
            SearchBar(
                query = searchText,
                onQueryChange = viewModel::onSearchTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Categorías - más destacadas
            Text(
                text = texto("categorias"), // CAMBIADO: Usar traducción
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            CategoriasRow(
                categorias = categorias,
                selectedCategory = selectedCategory,
                onCategorySelected = viewModel::onCategorySelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Resultados de búsqueda
            if (searchText.isNotEmpty() || selectedCategory != null) {
                Text(
                    text = "${texto("resultados")} (${recetas.size})", // CAMBIADO: Usar traducción
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Lista de recetas
            if (recetas.isEmpty() && (searchText.isNotEmpty() || selectedCategory != null)) {
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
                            Icons.Default.Search,
                            contentDescription = texto("sin_resultados"), // CAMBIADO: Usar traducción
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            texto("sin_resultados"), // CAMBIADO: Usar traducción
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (searchText.isNotEmpty()) {
                            Text(
                                text = "${texto("para_busqueda").replace("%s", searchText)}", // CAMBIADO: Usar traducción
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(recetas) { receta ->
                        RecetaCard(
                            receta = receta,
                            onToggleFavorito = { viewModel.toggleFavorito(receta) },
                            onVerReceta = { onNavigateToDetalleReceta(receta) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriasRow(
    categorias: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categorias) { categoria ->
            SuggestionChip(
                onClick = {
                    onCategorySelected(if (selectedCategory == categoria) null else categoria)
                },
                label = { Text(categoria) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = if (selectedCategory == categoria) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                border = if (selectedCategory == categoria) {
                    SuggestionChipDefaults.suggestionChipBorder(
                        borderColor = MaterialTheme.colorScheme.primary,
                        borderWidth = 2.dp
                    )
                } else {
                    null
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaBusqueda() {
    FastyRecipesTheme {
        // PantallaBusqueda(viewModel = ..., onBack = {})
    }
}