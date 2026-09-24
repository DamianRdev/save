package com.damianrdev.save.ui.reader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.damianrdev.save.ui.detail.BookmarkDetailViewModel
import com.damianrdev.save.ui.home.openUrlInCustomTabs
import com.damianrdev.save.ui.theme.EmeraldSuccess

enum class ReaderColorTheme(
    val label: String,
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color
) {
    LIGHT(
        label = "Claro",
        background = Color(0xFFFAF9F5),
        surface = Color(0xFFF0EFE9),
        textPrimary = Color(0xFF181A20),
        textSecondary = Color(0xFF5A6072),
        accent = Color(0xFF4F46E5)
    ),
    SEPIA(
        label = "Sepia",
        background = Color(0xFFF4ECD8),
        surface = Color(0xFFEAE0C8),
        textPrimary = Color(0xFF2C221E),
        textSecondary = Color(0xFF6E5A4F),
        accent = Color(0xFFB45309)
    ),
    DARK(
        label = "Oscuro",
        background = Color(0xFF080A10),
        surface = Color(0xFF141824),
        textPrimary = Color(0xFFF1F5F9),
        textSecondary = Color(0xFF94A3B8),
        accent = Color(0xFF6366F1)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: BookmarkDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val item by viewModel.bookmark.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showStyleControls by remember { mutableStateOf(false) }
    var fontSizeSp by remember { mutableIntStateOf(18) }
    var lineHeightMultiplier by remember { mutableFloatStateOf(1.65f) }
    var selectedTheme by remember { mutableStateOf(ReaderColorTheme.DARK) }

    val bookmark = item?.bookmark

    // Calculate reading progress and persist it when user scrolls
    val progress = if (scrollState.maxValue > 0) {
        (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
    } else {
        1f
    }

    LaunchedEffect(scrollState.value) {
        if (scrollState.maxValue > 0) {
            viewModel.updateReadingProgress(progress)
        }
    }

    if (bookmark == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cargando modo lector…")
        }
        return
    }

    val fullBody = bookmark.offlineHtmlContent?.ifBlank { null } ?: bookmark.description
    val isOfflineSaved = bookmark.offlineStatus == "AVAILABLE" && !bookmark.offlineHtmlContent.isNullOrBlank()

    Scaffold(
        containerColor = selectedTheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = bookmark.sourceDomain,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = selectedTheme.accent
                            )
                            Text(
                                text = "Modo Lector • ~${bookmark.readingTimeMinutes.coerceAtLeast(1)} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = selectedTheme.textSecondary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = selectedTheme.textPrimary
                            )
                        }
                    },
                    actions = {
                        // Offline download / status button
                        IconButton(
                            onClick = {
                                viewModel.toggleOfflineDownload()
                                Toast.makeText(
                                    context,
                                    if (isOfflineSaved) "Copia offline eliminada" else "Descargando copia offline…",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = if (isOfflineSaved) Icons.Filled.CloudDone else Icons.Filled.CloudDownload,
                                contentDescription = "Guardar Offline",
                                tint = if (isOfflineSaved) EmeraldSuccess else selectedTheme.textSecondary
                            )
                        }

                        // Typography & Theme Controls Toggle
                        IconButton(onClick = { showStyleControls = !showStyleControls }) {
                            Icon(
                                imageVector = Icons.Outlined.FormatSize,
                                contentDescription = "Apariencia de lectura",
                                tint = if (showStyleControls) selectedTheme.accent else selectedTheme.textPrimary
                            )
                        }

                        // Mark as Read toggle
                        IconButton(onClick = { viewModel.toggleRead() }) {
                            Icon(
                                imageVector = if (bookmark.isRead) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = "Marcar como leído",
                                tint = if (bookmark.isRead) EmeraldSuccess else selectedTheme.textSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = selectedTheme.background
                    )
                )

                // Live Reading Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = selectedTheme.accent,
                    trackColor = selectedTheme.surface
                )

                // Expandable Typography & Theme Control Bar
                AnimatedVisibility(visible = showStyleControls) {
                    Surface(
                        color = selectedTheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Theme selector (Claro / Sepia / Oscuro)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tema:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = selectedTheme.textSecondary,
                                    modifier = Modifier.width(56.dp)
                                )
                                ReaderColorTheme.entries.forEach { themeOption ->
                                    val isSelected = selectedTheme == themeOption
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(themeOption.background)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) selectedTheme.accent else selectedTheme.textSecondary.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedTheme = themeOption }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = themeOption.label,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = themeOption.textPrimary
                                        )
                                    }
                                }
                            }

                            // Font size & Line height controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Fuente:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = selectedTheme.textSecondary
                                    )
                                    OutlinedButton(
                                        onClick = { fontSizeSp = (fontSizeSp - 2).coerceAtLeast(14) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("A-", color = selectedTheme.textPrimary)
                                    }
                                    Text(
                                        text = "${fontSizeSp}sp",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = selectedTheme.textPrimary
                                    )
                                    OutlinedButton(
                                        onClick = { fontSizeSp = (fontSizeSp + 2).coerceAtMost(26) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("A+", color = selectedTheme.textPrimary)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Espaciado:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = selectedTheme.textSecondary
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            lineHeightMultiplier = when (lineHeightMultiplier) {
                                                1.45f -> 1.65f
                                                1.65f -> 1.9f
                                                else -> 1.45f
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        val label = when (lineHeightMultiplier) {
                                            1.45f -> "Compacto"
                                            1.65f -> "Normal"
                                            else -> "Amplio"
                                        }
                                        Text(label, color = selectedTheme.textPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            // Offline badge & Author
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isOfflineSaved) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(EmeraldSuccess.copy(alpha = 0.16f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "✓ Copia Offline lista",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldSuccess
                        )
                    }
                }
                if (!bookmark.author.isNullOrBlank()) {
                    Text(
                        text = "Por ${bookmark.author}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = selectedTheme.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Headline
            Text(
                text = bookmark.title,
                fontSize = (fontSizeSp + 6).sp,
                lineHeight = ((fontSizeSp + 6) * 1.25f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = selectedTheme.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cover Image if available
            if (!bookmark.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = bookmark.thumbnailUrl,
                    contentDescription = bookmark.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            HorizontalDivider(color = selectedTheme.textSecondary.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(20.dp))

            if (!fullBody.isNullOrBlank()) {
                val paragraphs = fullBody.split("\n\n")
                paragraphs.forEach { block ->
                    val trimmed = block.trim()
                    when {
                        trimmed.startsWith("## ") -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = trimmed.removePrefix("## "),
                                fontSize = (fontSizeSp + 3).sp,
                                lineHeight = ((fontSizeSp + 3) * 1.35f).sp,
                                fontWeight = FontWeight.Bold,
                                color = selectedTheme.textPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        trimmed.startsWith("> ") -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(selectedTheme.accent)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = trimmed.removePrefix("> "),
                                    fontSize = fontSizeSp.sp,
                                    lineHeight = (fontSizeSp * lineHeightMultiplier).sp,
                                    fontStyle = FontStyle.Italic,
                                    color = selectedTheme.textSecondary
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = trimmed,
                                fontSize = fontSizeSp.sp,
                                lineHeight = (fontSizeSp * lineHeightMultiplier).sp,
                                fontFamily = FontFamily.Serif,
                                color = selectedTheme.textPrimary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            } else {
                // Honest fallback when website blocks full text scraping
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = selectedTheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Vista simplificada parcial",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = selectedTheme.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Este sitio requiere interacción directa o inicio de sesión para mostrar el artículo completo. Puedes intentar descargar la copia offline nuevamente o abrir la publicación original.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = selectedTheme.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(color = selectedTheme.textSecondary.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(18.dp))

            // Footer Actions: Copy Text, Mark Read, Open Original Web Page
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Lectura Save", fullBody ?: bookmark.title))
                        Toast.makeText(context, "Contenido copiado al portapapeles", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar texto", color = selectedTheme.textPrimary)
                }

                Button(
                    onClick = { openUrlInCustomTabs(context, bookmark.originalUrl) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver original")
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
