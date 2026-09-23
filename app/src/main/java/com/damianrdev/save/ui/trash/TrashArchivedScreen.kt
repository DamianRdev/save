package com.damianrdev.save.ui.trash

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.damianrdev.save.domain.model.BookmarkWithDetails
import com.damianrdev.save.ui.components.EmptyStateView
import com.damianrdev.save.ui.theme.CoralRed
import com.damianrdev.save.ui.theme.EmeraldSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashArchivedScreen(
    viewModel: TrashArchivedViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (state.activeTab == TrashTab.TRASH) "Papelera" else "Archivados") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (state.activeTab == TrashTab.TRASH && state.trashItems.isNotEmpty()) {
                        TextButton(onClick = { viewModel.emptyTrash() }) {
                            Text("Vaciar papelera", color = CoralRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Tab Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.activeTab == TrashTab.TRASH,
                    onClick = { viewModel.setTab(TrashTab.TRASH) },
                    label = { Text("Papelera (${state.trashItems.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = state.activeTab == TrashTab.ARCHIVED,
                    onClick = { viewModel.setTab(TrashTab.ARCHIVED) },
                    label = { Text("Archivados (${state.archivedItems.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val currentItems = if (state.activeTab == TrashTab.TRASH) state.trashItems else state.archivedItems

            if (currentItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = if (state.activeTab == TrashTab.TRASH) Icons.Outlined.Delete else Icons.Outlined.Archive,
                        title = if (state.activeTab == TrashTab.TRASH) "La papelera está vacía" else "No hay enlaces archivados",
                        subtitle = if (state.activeTab == TrashTab.TRASH) "Los elementos eliminados se guardan aquí temporalmente." else "Archiva enlaces para mantener tu bandeja principal despejada."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 30.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(currentItems, key = { it.bookmark.id }) { item ->
                        TrashCard(
                            item = item,
                            isTrash = state.activeTab == TrashTab.TRASH,
                            onRestore = {
                                if (state.activeTab == TrashTab.TRASH) {
                                    viewModel.restoreItem(item.bookmark.id)
                                } else {
                                    viewModel.unarchiveItem(item.bookmark.id)
                                }
                            },
                            onDeletePermanently = {
                                viewModel.deletePermanently(item.bookmark.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrashCard(
    item: BookmarkWithDetails,
    isTrash: Boolean,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.bookmark.sourceDomain,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.bookmark.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRestore, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isTrash) Icons.Outlined.Restore else Icons.Outlined.Unarchive,
                        contentDescription = "Restaurar",
                        tint = EmeraldSuccess
                    )
                }

                if (isTrash) {
                    IconButton(onClick = onDeletePermanently, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteForever,
                            contentDescription = "Eliminar definitivamente",
                            tint = CoralRed
                        )
                    }
                }
            }
        }
    }
}
