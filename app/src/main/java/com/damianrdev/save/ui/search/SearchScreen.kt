package com.damianrdev.save.ui.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.damianrdev.save.ui.components.BookmarkCard
import com.damianrdev.save.ui.components.EmptyStateView
import com.damianrdev.save.ui.home.openUrlInCustomTabs

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Large Search Bar
        OutlinedTextField(
            value = state.filter.query,
            onValueChange = { viewModel.onQueryChange(it) },
            placeholder = { Text("Buscar por título, nota, tag, URL o dominio…") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (state.filter.query.isNotBlank()) {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Limpiar")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.filter.onlyFavorites,
                onClick = { viewModel.toggleOnlyFavorites() },
                label = { Text("Favoritos") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = state.filter.onlyUnread,
                onClick = { viewModel.toggleOnlyUnread() },
                label = { Text("No leídos") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )

            // Dynamic Collections filter chips
            state.collections.forEach { col ->
                val isSelected = state.filter.collectionId == col.id
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCollection(col.id) },
                    label = { Text(col.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Results Section
        if (state.results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Outlined.Search,
                    title = if (state.isSearching) "Sin resultados" else "Busca en tu bóveda",
                    subtitle = if (state.isSearching) "Prueba con otra palabra clave o quita los filtros." else "Escribe una palabra para encontrar enlaces por nota, etiqueta o contenido."
                )
            }
        } else {
            Text(
                text = "${state.results.size} resultados encontrados",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = state.results,
                    key = { it.bookmark.id }
                ) { item ->
                    BookmarkCard(
                        item = item,
                        isSelected = false,
                        isSelectionMode = false,
                        onClick = { onNavigateToDetail(item.bookmark.id) },
                        onLongClick = { },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(item.bookmark.id, item.bookmark.isFavorite)
                        },
                        onOpenUrl = {
                            openUrlInCustomTabs(context, item.bookmark.originalUrl)
                        }
                    )
                }
            }
        }
    }
}
