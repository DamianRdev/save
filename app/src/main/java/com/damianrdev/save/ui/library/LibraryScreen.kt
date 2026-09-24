package com.damianrdev.save.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.ViewHeadline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.damianrdev.save.domain.model.BookmarkWithDetails
import com.damianrdev.save.domain.model.SortOrder
import com.damianrdev.save.ui.components.BookmarkCard
import com.damianrdev.save.ui.components.EmptyStateView
import com.damianrdev.save.ui.home.HomeFilter
import com.damianrdev.save.ui.home.HomeViewModel
import com.damianrdev.save.ui.home.openUrlInCustomTabs
import com.damianrdev.save.ui.theme.CoralRed
import com.damianrdev.save.ui.theme.EmeraldSuccess

enum class LibraryDisplayMode {
    MAGAZINE, COMPACT, GRID
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: HomeViewModel,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var displayMode by remember { mutableStateOf(LibraryDisplayMode.MAGAZINE) }
    var sortOrder by remember { mutableStateOf(SortOrder.NEWEST) }
    var showSortMenu by remember { mutableStateOf(false) }
    var onlyOfflineFilter by remember { mutableStateOf(false) }
    var onlyReadFilter by remember { mutableStateOf(false) }

    val displayedBookmarks = remember(state.bookmarks, sortOrder, onlyOfflineFilter, onlyReadFilter) {
        val base = state.bookmarks.filter { item ->
            val matchesOffline = !onlyOfflineFilter || item.bookmark.offlineStatus == "AVAILABLE"
            val matchesRead = !onlyReadFilter || item.bookmark.isRead
            matchesOffline && matchesRead
        }
        when (sortOrder) {
            SortOrder.NEWEST -> base.sortedByDescending { it.bookmark.createdAt }
            SortOrder.OLDEST -> base.sortedBy { it.bookmark.createdAt }
            SortOrder.TITLE -> base.sortedBy { it.bookmark.title.lowercase() }
            SortOrder.LAST_OPENED -> base.sortedByDescending { it.bookmark.lastOpenedAt ?: 0L }
            SortOrder.DOMAIN -> base.sortedBy { it.bookmark.sourceDomain.lowercase() }
            SortOrder.READING_TIME -> base.sortedByDescending { it.bookmark.readingTimeMinutes }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (state.isSelectionMode) {
                TopAppBar(
                    title = { Text("${state.selectedBookmarkIds.size} seleccionados") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancelar")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.archiveSelected() }) {
                            Icon(imageVector = Icons.Outlined.Archive, contentDescription = "Archivar")
                        }
                        IconButton(onClick = { viewModel.deleteSelected() }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = CoralRed)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Tu Biblioteca",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Text(
                                text = "${displayedBookmarks.size} elementos • Orden: ${sortOrder.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        // Cycle Display Mode (Magazine -> Compact -> Grid)
                        IconButton(
                            onClick = {
                                displayMode = when (displayMode) {
                                    LibraryDisplayMode.MAGAZINE -> LibraryDisplayMode.COMPACT
                                    LibraryDisplayMode.COMPACT -> LibraryDisplayMode.GRID
                                    LibraryDisplayMode.GRID -> LibraryDisplayMode.MAGAZINE
                                }
                            }
                        ) {
                            val icon = when (displayMode) {
                                LibraryDisplayMode.MAGAZINE -> Icons.Outlined.ViewAgenda
                                LibraryDisplayMode.COMPACT -> Icons.Outlined.ViewHeadline
                                LibraryDisplayMode.GRID -> Icons.Outlined.GridView
                            }
                            Icon(imageVector = icon, contentDescription = "Cambiar vista", tint = MaterialTheme.colorScheme.primary)
                        }

                        // Sort Dropdown
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(imageVector = Icons.AutoMirrored.Outlined.Sort, contentDescription = "Ordenar")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                SortOrder.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.label) },
                                        trailingIcon = {
                                            if (sortOrder == option) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess)
                                            }
                                        },
                                        onClick = {
                                            sortOrder = option
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.activeFilter == HomeFilter.ALL && !onlyOfflineFilter && !onlyReadFilter,
                    onClick = {
                        onlyOfflineFilter = false
                        onlyReadFilter = false
                        viewModel.setFilter(HomeFilter.ALL)
                    },
                    label = { Text("Todos (${state.totalCount})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = state.activeFilter == HomeFilter.UNREAD,
                    onClick = {
                        onlyOfflineFilter = false
                        onlyReadFilter = false
                        viewModel.setFilter(HomeFilter.UNREAD)
                    },
                    label = { Text("Pendientes (${state.unreadCount})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = onlyReadFilter,
                    onClick = {
                        onlyReadFilter = !onlyReadFilter
                        if (onlyReadFilter) viewModel.setFilter(HomeFilter.ALL)
                    },
                    label = { Text("Leídos") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldSuccess,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = state.activeFilter == HomeFilter.FAVORITES,
                    onClick = {
                        onlyOfflineFilter = false
                        onlyReadFilter = false
                        viewModel.setFilter(HomeFilter.FAVORITES)
                    },
                    label = { Text("Favoritos (${state.favoritesCount})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = onlyOfflineFilter,
                    onClick = { onlyOfflineFilter = !onlyOfflineFilter },
                    label = { Text("📶 Offline") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldSuccess,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = state.activeFilter == HomeFilter.UNCATEGORIZED,
                    onClick = {
                        onlyOfflineFilter = false
                        onlyReadFilter = false
                        viewModel.setFilter(HomeFilter.UNCATEGORIZED)
                    },
                    label = { Text("Sin organizar") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }

            if (displayedBookmarks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        icon = Icons.Outlined.Bookmarks,
                        title = "No hay elementos en esta vista",
                        subtitle = "Comparte cualquier artículo, hilo o video desde otra aplicación hacia SAVE o cambia el filtro activo."
                    )
                }
            } else if (displayMode == LibraryDisplayMode.GRID) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedBookmarks, key = { it.bookmark.id }) { item ->
                        GridBookmarkCard(
                            item = item,
                            onClick = { onNavigateToDetail(item.bookmark.id) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedBookmarks, key = { it.bookmark.id }) { item ->
                        val isSelected = state.selectedBookmarkIds.contains(item.bookmark.id)
                        BookmarkCard(
                            item = item,
                            isSelected = isSelected,
                            isSelectionMode = state.isSelectionMode,
                            isCompact = displayMode == LibraryDisplayMode.COMPACT,
                            onClick = {
                                if (state.isSelectionMode) {
                                    viewModel.toggleSelectBookmark(item.bookmark.id)
                                } else {
                                    onNavigateToDetail(item.bookmark.id)
                                }
                            },
                            onLongClick = { viewModel.startSelectionMode(item.bookmark.id) },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(item.bookmark.id, item.bookmark.isFavorite)
                            },
                            onOpenUrl = { openUrlInCustomTabs(context, item.bookmark.originalUrl) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridBookmarkCard(
    item: BookmarkWithDetails,
    onClick: () -> Unit
) {
    val b = item.bookmark
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            if (!b.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = b.thumbnailUrl,
                    contentDescription = b.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = b.sourceDomain.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = b.sourceDomain,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = b.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "~${b.readingTimeMinutes.coerceAtLeast(1)} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (b.offlineStatus == "AVAILABLE") {
                        Text(
                            text = "📶",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
