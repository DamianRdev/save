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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.damianrdev.save.domain.model.SortOrder
import com.damianrdev.save.ui.components.BookmarkCard
import com.damianrdev.save.ui.components.EmptyStateView
import com.damianrdev.save.ui.home.openUrlInCustomTabs
import com.damianrdev.save.ui.theme.EmeraldSuccess

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar + Sort Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.filter.query,
                onValueChange = { viewModel.onQueryChange(it) },
                placeholder = { Text("Buscar por título, nota, tag, autor o URL…") },
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { viewModel.commitCurrentSearchToHistory() }
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                modifier = Modifier.weight(1f)
            )

            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Sort,
                        contentDescription = "Ordenar resultados",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = { Text(order.label) },
                            trailingIcon = {
                                if (state.filter.sortOrder == order) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess)
                                }
                            },
                            onClick = {
                                viewModel.setSortOrder(order)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Recent Searches Row (shown when query is empty and history exists)
        if (state.filter.query.isBlank() && state.recentSearches.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Búsquedas recientes",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { viewModel.clearRecentSearches() },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Text("Limpiar", style = MaterialTheme.typography.labelSmall)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.recentSearches.forEach { term ->
                    AssistChip(
                        onClick = { viewModel.onQueryChange(term) },
                        label = { Text(term) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.History, contentDescription = null)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Filter Chips Row (Status, Offline, Content Type, Collections)
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
                label = { Text("Pendientes") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = state.filter.onlyOffline,
                onClick = { viewModel.toggleOnlyOffline() },
                label = { Text("📶 Offline") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldSuccess,
                    selectedLabelColor = Color.White
                )
            )

            // Content Type Chips
            val contentTypes = listOf(
                "ARTICLE" to "📄 Artículos",
                "VIDEO" to "🎥 Videos",
                "TWEET" to "💬 Social",
                "CODE" to "💻 Código",
                "PRODUCT" to "🛍️ Compras"
            )
            contentTypes.forEach { (code, label) ->
                FilterChip(
                    selected = state.filter.contentType == code,
                    onClick = { viewModel.selectContentType(code) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }

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

        Spacer(modifier = Modifier.height(10.dp))

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
                    title = if (state.isSearching) "Sin resultados" else "Busca en tu biblioteca",
                    subtitle = if (state.isSearching) "Prueba con otra palabra clave o quita los filtros." else "Escribe una palabra para encontrar enlaces por título, nota, autor, etiqueta o contenido."
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.results.size} resultados • ${state.filter.sortOrder.label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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
                        onClick = {
                            viewModel.commitCurrentSearchToHistory()
                            onNavigateToDetail(item.bookmark.id)
                        },
                        onLongClick = { },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(item.bookmark.id, item.bookmark.isFavorite)
                        },
                        onOpenUrl = {
                            viewModel.commitCurrentSearchToHistory()
                            openUrlInCustomTabs(context, item.bookmark.originalUrl)
                        }
                    )
                }
            }
        }
    }
}
