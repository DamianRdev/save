package com.damianrdev.save.ui.home

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.ViewHeadline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damianrdev.save.core.common.UrlSanitizer
import com.damianrdev.save.ui.components.BatchActionBar
import com.damianrdev.save.ui.components.BookmarkCard
import com.damianrdev.save.ui.components.EmptyStateView
import com.damianrdev.save.ui.theme.AmberWarning
import com.damianrdev.save.ui.theme.CoralRed
import com.damianrdev.save.ui.theme.IndigoAccent
import com.damianrdev.save.ui.theme.VioletAccent
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMoveDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    var clipboardUrl by remember { mutableStateOf<String?>(null) }
    var clipboardRawText by remember { mutableStateOf<String?>(null) }
    var clipboardDomain by remember { mutableStateOf<String?>(null) }
    var isClipboardDismissed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        val clipData = clipboard?.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val text = clipData.getItemAt(0)?.coerceToText(context)?.toString()
            val url = UrlSanitizer.extractUrl(text)
            if (url != null) {
                clipboardUrl = url
                clipboardRawText = text
                clipboardDomain = UrlSanitizer.extractDomain(url)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Save 2.0 Header Title + View Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(IndigoAccent, VioletAccent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "S",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Biblioteca inteligente • Local-First",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Magazine vs Compact View Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.toggleViewMode() }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (state.isCompactView) Icons.Outlined.ViewAgenda else Icons.Outlined.ViewHeadline,
                            contentDescription = "Cambiar vista",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isCompactView) "Magazine" else "Compacta",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bento Stats Interactive Dashboard Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BentoStatCard(
                    title = "Guardados",
                    count = state.totalCount,
                    accentColor = IndigoAccent,
                    isSelected = state.activeFilter == HomeFilter.ALL,
                    onClick = { viewModel.setFilter(HomeFilter.ALL) },
                    modifier = Modifier.weight(1f)
                )
                BentoStatCard(
                    title = "Por leer",
                    count = state.unreadCount,
                    accentColor = AmberWarning,
                    isSelected = state.activeFilter == HomeFilter.UNREAD,
                    onClick = { viewModel.setFilter(HomeFilter.UNREAD) },
                    modifier = Modifier.weight(1f)
                )
                BentoStatCard(
                    title = "Favoritos",
                    count = state.favoritesCount,
                    accentColor = CoralRed,
                    isSelected = state.activeFilter == HomeFilter.FAVORITES,
                    onClick = { viewModel.setFilter(HomeFilter.FAVORITES) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Clipboard Smart Detection Banner
            AnimatedVisibility(visible = clipboardUrl != null && !isClipboardDismissed) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✨ Enlace detectado en portapapeles",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = clipboardUrl ?: clipboardDomain ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = {
                                    clipboardUrl?.let { url ->
                                        viewModel.quickSaveUrl(url = url, rawText = clipboardRawText)
                                        isClipboardDismissed = true
                                    }
                                }
                            ) {
                                Text("Guardar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = { isClipboardDismissed = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Descartar",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips (Todos, Por leer, Favoritos, Sin clasificar)
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
                    selected = state.activeFilter == HomeFilter.UNREAD,
                    onClick = { viewModel.setFilter(HomeFilter.UNREAD) },
                    label = { Text("Por leer") },
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

                FilterChip(
                    selected = state.activeFilter == HomeFilter.UNCATEGORIZED,
                    onClick = { viewModel.setFilter(HomeFilter.UNCATEGORIZED) },
                    label = { Text("Sin Clasificar") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )

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

            Spacer(modifier = Modifier.height(10.dp))

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
                        title = "Tu biblioteca está lista",
                        subtitle = "Toca el botón '+ Nuevo' o comparte cualquier enlace desde Threads, X, YouTube, Instagram o Chrome."
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

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                when (dismissValue) {
                                    SwipeToDismissBoxValue.EndToStart -> {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.moveToTrash(item.bookmark.id)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Enlace movido a la papelera",
                                                actionLabel = "Deshacer",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.restoreBookmark(item.bookmark.id)
                                            }
                                        }
                                        true
                                    }
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.archiveBookmark(item.bookmark.id)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Enlace archivado",
                                                actionLabel = "Deshacer",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.unarchiveBookmark(item.bookmark.id)
                                            }
                                        }
                                        true
                                    }
                                    SwipeToDismissBoxValue.Settled -> false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = !state.isSelectionMode,
                            enableDismissFromEndToStart = !state.isSelectionMode,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    targetValue = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                                        SwipeToDismissBoxValue.Settled -> Color.Transparent
                                    },
                                    label = "swipe_color"
                                )
                                val icon = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.EndToStart -> Icons.Outlined.Delete
                                    SwipeToDismissBoxValue.StartToEnd -> Icons.Outlined.Archive
                                    else -> null
                                }
                                val alignment = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                    else -> Alignment.Center
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(color)
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = alignment
                                ) {
                                    if (icon != null) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                                MaterialTheme.colorScheme.onErrorContainer
                                            else
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        ) {
                            BookmarkCard(
                                item = item,
                                isSelected = isSelected,
                                isSelectionMode = state.isSelectionMode,
                                isCompact = state.isCompactView,
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
        }

        // Floating Action Button (+ Nuevo Enlace)
        AnimatedVisibility(
            visible = !state.isSelectionMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                icon = {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo enlace")
                },
                text = {
                    Text(
                        text = "Nuevo",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
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

        // Add New Bookmark Manually Dialog
        if (showAddDialog) {
            var manualUrl by remember { mutableStateOf(clipboardUrl ?: "") }
            var manualTitle by remember { mutableStateOf("") }
            var manualNote by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Text(
                        text = "Guardar nuevo enlace",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = manualUrl,
                            onValueChange = { manualUrl = it },
                            label = { Text("URL o texto con enlace") },
                            placeholder = { Text("https://www.threads.net/...") },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = manualTitle,
                            onValueChange = { manualTitle = it },
                            label = { Text("Título (Opcional - se detecta solo)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = manualNote,
                            onValueChange = { manualNote = it },
                            label = { Text("Nota personal (Opcional)") },
                            singleLine = false,
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val extracted = UrlSanitizer.extractUrl(manualUrl) ?: manualUrl.trim()
                            if (extracted.isNotBlank()) {
                                val formattedUrl = if (extracted.startsWith("http")) extracted else "https://$extracted"
                                viewModel.quickSaveUrl(
                                    url = formattedUrl,
                                    rawText = manualUrl,
                                    title = manualTitle.ifBlank { null },
                                    note = manualNote.ifBlank { null }
                                )
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Guardar enlace", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancelar")
                    }
                }
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (state.isSelectionMode) 80.dp else 16.dp)
        )
    }
}

@Composable
private fun BentoStatCard(
    title: String,
    count: Int,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                accentColor.copy(alpha = 0.14f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    ),
                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
