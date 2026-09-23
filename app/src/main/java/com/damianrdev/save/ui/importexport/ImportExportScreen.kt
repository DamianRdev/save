package com.damianrdev.save.ui.importexport

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Html
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.damianrdev.save.domain.repository.ExportFormat
import com.damianrdev.save.ui.theme.EmeraldSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    viewModel: ImportExportViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var pendingExportFormat by remember { mutableStateOf<ExportFormat?>(null) }
    var pendingImportFormat by remember { mutableStateOf<ExportFormat?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri != null && pendingExportFormat != null) {
            viewModel.exportToFile(context, uri, pendingExportFormat!!)
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null && pendingImportFormat != null) {
            viewModel.importFromFile(context, uri, pendingImportFormat!!)
        }
    }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Importar y Exportar", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Tus datos te pertenecen",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Exporta tu biblioteca completa en cualquier momento o importa marcadores desde Chrome, Firefox, Raindrop o Pocket sin bloqueos ni formatos cerrados.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (state.isProcessing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(
                        text = "  Procesando biblioteca local…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Section 1: JSON Full Backup
            FormatSectionCard(
                icon = Icons.Outlined.Code,
                title = "Backup Completo (JSON)",
                description = "Incluye todos tus enlaces, colecciones, etiquetas, notas personales y fechas. Recomendado para copias de seguridad locales.",
                onExport = {
                    pendingExportFormat = ExportFormat.JSON
                    createDocumentLauncher.launch("save_backup_${System.currentTimeMillis()}.json")
                },
                onImport = {
                    pendingImportFormat = ExportFormat.JSON
                    openDocumentLauncher.launch(arrayOf("application/json", "text/*"))
                }
            )

            // Section 2: CSV Spreadsheet
            FormatSectionCard(
                icon = Icons.Outlined.TableChart,
                title = "Hojas de Cálculo (CSV)",
                description = "Ideal para abrir en Microsoft Excel, Google Sheets, Notion o procesar con tus propios scripts.",
                onExport = {
                    pendingExportFormat = ExportFormat.CSV
                    createDocumentLauncher.launch("save_bookmarks_${System.currentTimeMillis()}.csv")
                },
                onImport = {
                    pendingImportFormat = ExportFormat.CSV
                    openDocumentLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/*"))
                }
            )

            // Section 3: HTML Netscape Bookmarks
            FormatSectionCard(
                icon = Icons.Outlined.Html,
                title = "Marcadores Web (HTML)",
                description = "Formato estándar compatible con Chrome, Firefox, Safari, Edge, Raindrop y Pocket.",
                onExport = {
                    pendingExportFormat = ExportFormat.HTML
                    createDocumentLauncher.launch("bookmarks_${System.currentTimeMillis()}.html")
                },
                onImport = {
                    pendingImportFormat = ExportFormat.HTML
                    openDocumentLauncher.launch(arrayOf("text/html", "text/*"))
                }
            )
        }
    }
}

@Composable
fun FormatSectionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "  $title",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onExport,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("  Exportar")
                }

                OutlinedButton(
                    onClick = onImport,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("  Importar")
                }
            }
        }
    }
}
