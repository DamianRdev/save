package com.damianrdev.save.ui.importexport

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damianrdev.save.domain.repository.BookmarkRepository
import com.damianrdev.save.domain.repository.ExportFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject

data class ImportExportUiState(
    val isProcessing: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportExportUiState())
    val uiState: StateFlow<ImportExportUiState> = _uiState.asStateFlow()

    fun exportToFile(context: Context, uri: Uri, format: ExportFormat) {
        _uiState.update { it.copy(isProcessing = true, successMessage = null, errorMessage = null) }

        viewModelScope.launch {
            try {
                val data = bookmarkRepository.exportData(format)
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        OutputStreamWriter(os).use { writer ->
                            writer.write(data)
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        successMessage = "Biblioteca exportada correctamente en formato ${format.name}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Error al exportar: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun importFromFile(context: Context, uri: Uri, format: ExportFormat) {
        _uiState.update { it.copy(isProcessing = true, successMessage = null, errorMessage = null) }

        viewModelScope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BufferedReader(InputStreamReader(stream)).use { reader ->
                            reader.readText()
                        }
                    } ?: ""
                }

                if (content.isBlank()) {
                    _uiState.update { it.copy(isProcessing = false, errorMessage = "El archivo seleccionado está vacío") }
                    return@launch
                }

                val count = bookmarkRepository.importData(content, format)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        successMessage = "Se importaron $count enlaces correctamente"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Error al importar archivo: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
