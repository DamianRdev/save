package com.damianrdev.save.ui.home

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.damianrdev.save.ui.components.BatchActionBar
import com.damianrdev.save.ui.components.BookmarkCard
import com.damianrdev.save.ui.components.EmptyStateView

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showMoveDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${state.bookmarks.size} enlaces guardados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Filter Chips (Todos, Sin clasificar, Favoritos)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.activeFilter == HomeFilter.ALL,
                    onClick = { viewModel.setFilter(HomeFilter.ALL) },
                    label = { Text("Todos") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = state.activeFilter == HomeFilter.UNCATEGORIZED,
                    onClick = { viewModel.setFilter(HomeFilter.UNCATEGORIZED) },
                    label = { Text("Sin Clasificar") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = state.activeFilter == HomeFilter.FAVORITES,
                    onClick = { viewModel.setFilter(HomeFilter.FAVORITES) },
                    label = { Text("Favoritos") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )

                // Optional collection filter reset or specific chips
                if (state.selectedCollectionId != null) {
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.selectCollection(null) },
                        label = { Text("Filtro: Colección ✕") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main List or Empty State
            if (state.bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Outlined.BookmarkBorder,
                        title = "Tu biblioteca está vacía",
                        subtitle = "Comparte cualquier enlace desde YouTube, Instagram, TikTok o Chrome tocando 'Compartir' y seleccionando Save."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = state.bookmarks,
                        key = { it.bookmark.id }
                    ) { item ->
                        val isSelected = state.selectedBookmarkIds.contains(item.bookmark.id)

                        BookmarkCard(
                            item = item,
                            isSelected = isSelected,
                            isSelectionMode = state.isSelectionMode,
                            onClick = {
                                if (state.isSelectionMode) {
                                    viewModel.toggleSelectBookmark(item.bookmark.id)
                                } else {
                                    onNavigateToDetail(item.bookmark.id)
                                }
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (!state.isSelectionMode) {
                                    viewModel.startSelectionMode(item.bookmark.id)
                                } else {
                                    viewModel.toggleSelectBookmark(item.bookmark.id)
                                }
                            },
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

        // Floating Batch Action Bar
        AnimatedVisibility(
            visible = state.isSelectionMode,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BatchActionBar(
                selectedCount = state.selectedBookmarkIds.size,
                onClose = { viewModel.clearSelection() },
                onSelectAll = { viewModel.selectAll() },
                onMoveToCollection = { showMoveDialog = true },
                onArchive = { viewModel.archiveSelected() },
                onDelete = { viewModel.deleteSelected() }
            )
        }

        // Move to collection dialog
        if (showMoveDialog) {
            AlertDialog(
                onDismissRequest = { showMoveDialog = false },
                title = { Text("Mover a colección") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    viewModel.moveSelectedToCollection(null)
                                    showMoveDialog = false
                                }
                                .padding(12.dp)
                        ) {
                            Text("Sin colección (Quitar de colección)")
                        }

                        state.collections.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        viewModel.moveSelectedToCollection(col.id)
                                        showMoveDialog = false
                                    }
                                    .padding(12.dp)
                            ) {
                                Text(col.name)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMoveDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

fun openUrlInCustomTabs(context: Context, url: String) {
    try {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (_: Exception) {
        // Fallback
    }
}
