package com.damianrdev.save.ui.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.damianrdev.save.ui.theme.EmeraldSuccess

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToTrash: () -> Unit,
    onNavigateToImportExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferences.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    var showTokenDialog by remember { mutableStateOf(false) }
    var tokenInput by remember(prefs.githubToken) { mutableStateOf(prefs.githubToken ?: "") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ajustes",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Section: In-App Updates from GitHub
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Outlined.SystemUpdate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.padding(start = 14.dp)) {
                            Text(
                                text = "Actualizaciones de Save",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Versión actual: v${viewModel.currentVersion}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (updateState is UpdateState.Checking) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Button(
                            onClick = { viewModel.checkForUpdates() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Buscar")
                        }
                    }
                }

                when (val state = updateState) {
                    is UpdateState.UpToDate -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Tienes instalada la versión más reciente.",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSuccess
                        )
                    }
                    is UpdateState.Error -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is UpdateState.Downloading -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Descargando actualización: ${(state.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (!prefs.githubToken.isNullOrBlank()) "✓ Token configurado (Repo privado)" else "Repo público (o configurar Token)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (!prefs.githubToken.isNullOrBlank()) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = { showTokenDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Configurar Token", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Dialog to configure GitHub Personal Access Token (for private repos)
        if (showTokenDialog) {
            AlertDialog(
                onDismissRequest = { showTokenDialog = false },
                title = { Text("Token de GitHub (Repo Privado)") },
                text = {
                    Column {
                        Text(
                            text = "Si tu repositorio en GitHub es privado, genera un Personal Access Token (classic o fine-grained con permiso de lectura de repo) y pégalo aquí. Si haces tu repositorio público en GitHub (Settings > Danger Zone), no necesitas ningún token.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = tokenInput,
                            onValueChange = { tokenInput = it },
                            placeholder = { Text("ghp_...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.setGithubToken(tokenInput.ifBlank { null })
                            showTokenDialog = false
                        }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.setGithubToken(null)
                            tokenInput = ""
                            showTokenDialog = false
                        }
                    ) {
                        Text("Eliminar")
                    }
                }
            )
        }

        // Dialog when update is found on GitHub
        if (updateState is UpdateState.Available) {
            val info = (updateState as UpdateState.Available).info
            AlertDialog(
                onDismissRequest = { viewModel.resetUpdateState() },
                title = { Text("Nueva versión disponible: v${info.versionName}") },
                text = {
                    Column {
                        Text(
                            text = "Novedades:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = info.releaseNotes, style = MaterialTheme.typography.bodyMedium)
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.downloadAndInstallUpdate(info.apkUrl) }) {
                        Text("Descargar e Instalar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.resetUpdateState() }) {
                        Text("Más tarde")
                    }
                }
            )
        }

        // General Navigation Items: Papelera, Importar/Exportar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column {
                SettingsRowItem(
                    icon = Icons.Outlined.Sync,
                    title = "Importar y Exportar biblioteca",
                    subtitle = "Copia de seguridad en JSON, CSV o HTML",
                    onClick = onNavigateToImportExport
                )
                SettingsRowItem(
                    icon = Icons.Outlined.Delete,
                    title = "Papelera y Archivados",
                    subtitle = "Recuperar o purgar enlaces eliminados",
                    onClick = onNavigateToTrash
                )
            }
        }

        // Section: Navegación & Comportamiento
        Text(
            text = "Comportamiento",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SettingsSwitchRow(
                    title = "Abrir con Chrome Custom Tabs",
                    subtitle = "Abre enlaces dentro de la app manteniendo tus sesiones",
                    checked = prefs.useCustomTabs,
                    onCheckedChange = { viewModel.setUseCustomTabs(it) }
                )

                SettingsSwitchRow(
                    title = "Limpiar parámetros de rastreo",
                    subtitle = "Remueve utm_*, si, igsh y fbclid automáticamente al guardar",
                    checked = prefs.cleanTrackingParams,
                    onCheckedChange = { viewModel.setCleanTrackingParams(it) }
                )

                SettingsSwitchRow(
                    title = "Extracción automática de metadatos",
                    subtitle = "Obtiene título, autor y modo lector en segundo plano",
                    checked = prefs.autoFetchMetadata,
                    onCheckedChange = { viewModel.setAutoFetchMetadata(it) }
                )

                SettingsSwitchRow(
                    title = "Descargas offline solo por Wi-Fi",
                    subtitle = "Ahorra datos móviles al guardar copias para el Modo Lector",
                    checked = prefs.wifiOnlyOffline,
                    onCheckedChange = { viewModel.setWifiOnlyOffline(it) }
                )
            }
        }

        // Section: Privacidad Local-First & Eliminación Total de Datos
        var showDeleteAllDialog by remember { mutableStateOf(false) }

        Text(
            text = "Privacidad Real y Control de Datos",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = EmeraldSuccess
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.Shield, contentDescription = null, tint = EmeraldSuccess)
                    Text(
                        text = "  100% Local y Privado",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Sin cuentas obligatorias ni contraseñas.\n• Sin conexión a servidores propietarios ni rastreadores.\n• Base de datos SQLite alojada exclusivamente en tu dispositivo.\n• Tus datos son completamente exportables e importables en JSON, CSV y HTML.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                androidx.compose.material3.OutlinedButton(
                    onClick = { showDeleteAllDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text("Eliminar todos los datos de SAVE", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = { Text("¿Eliminar toda la biblioteca?") },
                text = {
                    Text(
                        "Esta acción eliminará permanentemente todos los enlaces guardados, copias offline y búsquedas recientes de este dispositivo. Te recomendamos exportar una copia de seguridad en JSON antes de continuar."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteAllData()
                            showDeleteAllDialog = false
                        }
                    ) {
                        Text("Sí, eliminar todo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Version footer
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Save v${viewModel.currentVersion} — Local-First Personal Vault",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = EmeraldSuccess,
                checkedTrackColor = EmeraldSuccess.copy(alpha = 0.3f)
            )
        )
    }
}
