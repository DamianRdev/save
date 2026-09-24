package com.damianrdev.save.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damianrdev.save.data.datastore.UserPreferences
import com.damianrdev.save.data.datastore.UserPreferencesRepository
import com.damianrdev.save.data.updater.AppUpdateManager
import com.damianrdev.save.data.updater.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data object UpToDate : UpdateState
    data class Downloading(val progress: Float) : UpdateState
    data class Error(val message: String) : UpdateState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val bookmarkRepository: com.damianrdev.save.domain.repository.BookmarkRepository,
    private val appUpdateManager: AppUpdateManager
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    val currentVersion: String
        get() = appUpdateManager.getCurrentVersionName()

    val preferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun checkForUpdates() {
        _updateState.value = UpdateState.Checking
        viewModelScope.launch {
            appUpdateManager.checkForUpdates().fold(
                onSuccess = { info ->
                    if (info != null) {
                        _updateState.value = UpdateState.Available(info)
                    } else {
                        _updateState.value = UpdateState.UpToDate
                    }
                },
                onFailure = { error ->
                    _updateState.value = UpdateState.Error(error.localizedMessage ?: "Error al comprobar actualizaciones")
                }
            )
        }
    }

    fun downloadAndInstallUpdate(apkUrl: String) {
        _updateState.value = UpdateState.Downloading(0f)
        viewModelScope.launch {
            appUpdateManager.downloadAndInstall(apkUrl) { progress ->
                _updateState.value = UpdateState.Downloading(progress)
            }.onFailure { error ->
                _updateState.value = UpdateState.Error(error.localizedMessage ?: "Fallo al descargar la actualización")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }

    fun setUseCustomTabs(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setUseCustomTabs(enabled)
        }
    }

    fun setCleanTrackingParams(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setCleanTrackingParams(enabled)
        }
    }

    fun setAutoFetchMetadata(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setAutoFetchMetadata(enabled)
        }
    }

    fun setWifiOnlyOffline(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setWifiOnlyOffline(enabled)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun setGithubToken(token: String?) {
        viewModelScope.launch {
            userPreferencesRepository.setGithubToken(token)
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            bookmarkRepository.deleteAllUserData()
            userPreferencesRepository.clearRecentSearches()
        }
    }
}
