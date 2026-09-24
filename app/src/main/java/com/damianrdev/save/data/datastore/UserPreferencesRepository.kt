package com.damianrdev.save.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val useCustomTabs: Boolean = true,
    val themeMode: String = "AMOLED_BLACK", // SYSTEM, LIGHT, DARK, AMOLED_BLACK
    val defaultViewMode: String = "MAGAZINE", // MAGAZINE, COMPACT, GRID
    val defaultSortOrder: String = "NEWEST",
    val wifiOnlyOffline: Boolean = false,
    val defaultCollectionId: Long? = null,
    val cleanTrackingParams: Boolean = true,
    val autoFetchMetadata: Boolean = true,
    val readerFontSizeSp: Int = 18,
    val readerTheme: String = "DARK", // LIGHT, SEPIA, DARK
    val recentSearches: List<String> = emptyList(),
    val githubToken: String? = null
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_USE_CUSTOM_TABS = booleanPreferencesKey("use_custom_tabs")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DEFAULT_VIEW_MODE = stringPreferencesKey("default_view_mode")
        val KEY_DEFAULT_SORT_ORDER = stringPreferencesKey("default_sort_order")
        val KEY_WIFI_ONLY_OFFLINE = booleanPreferencesKey("wifi_only_offline")
        val KEY_DEFAULT_COLLECTION_ID = longPreferencesKey("default_collection_id")
        val KEY_CLEAN_TRACKING_PARAMS = booleanPreferencesKey("clean_tracking_params")
        val KEY_AUTO_FETCH_METADATA = booleanPreferencesKey("auto_fetch_metadata")
        val KEY_READER_FONT_SIZE = stringPreferencesKey("reader_font_size")
        val KEY_READER_THEME = stringPreferencesKey("reader_theme")
        val KEY_RECENT_SEARCHES = stringPreferencesKey("recent_searches")
        val KEY_GITHUB_TOKEN = stringPreferencesKey("github_token")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        val useCustomTabs = preferences[KEY_USE_CUSTOM_TABS] ?: true
        val themeMode = preferences[KEY_THEME_MODE] ?: "AMOLED_BLACK"
        val defaultViewMode = preferences[KEY_DEFAULT_VIEW_MODE] ?: "MAGAZINE"
        val defaultSortOrder = preferences[KEY_DEFAULT_SORT_ORDER] ?: "NEWEST"
        val wifiOnlyOffline = preferences[KEY_WIFI_ONLY_OFFLINE] ?: false
        val defaultCollectionId = preferences[KEY_DEFAULT_COLLECTION_ID]
        val cleanTrackingParams = preferences[KEY_CLEAN_TRACKING_PARAMS] ?: true
        val autoFetchMetadata = preferences[KEY_AUTO_FETCH_METADATA] ?: true
        val readerFontSize = preferences[KEY_READER_FONT_SIZE]?.toIntOrNull() ?: 18
        val readerTheme = preferences[KEY_READER_THEME] ?: "DARK"
        val recentSearches = preferences[KEY_RECENT_SEARCHES]
            ?.split("||")
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        val githubToken = preferences[KEY_GITHUB_TOKEN]

        UserPreferences(
            useCustomTabs = useCustomTabs,
            themeMode = themeMode,
            defaultViewMode = defaultViewMode,
            defaultSortOrder = defaultSortOrder,
            wifiOnlyOffline = wifiOnlyOffline,
            defaultCollectionId = defaultCollectionId,
            cleanTrackingParams = cleanTrackingParams,
            autoFetchMetadata = autoFetchMetadata,
            readerFontSizeSp = readerFontSize,
            readerTheme = readerTheme,
            recentSearches = recentSearches,
            githubToken = githubToken
        )
    }

    suspend fun setUseCustomTabs(enabled: Boolean) {
        dataStore.edit { it[KEY_USE_CUSTOM_TABS] = enabled }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setDefaultViewMode(mode: String) {
        dataStore.edit { it[KEY_DEFAULT_VIEW_MODE] = mode }
    }

    suspend fun setDefaultSortOrder(order: String) {
        dataStore.edit { it[KEY_DEFAULT_SORT_ORDER] = order }
    }

    suspend fun setWifiOnlyOffline(enabled: Boolean) {
        dataStore.edit { it[KEY_WIFI_ONLY_OFFLINE] = enabled }
    }

    suspend fun setReaderFontSize(sizeSp: Int) {
        dataStore.edit { it[KEY_READER_FONT_SIZE] = sizeSp.coerceIn(14, 26).toString() }
    }

    suspend fun setReaderTheme(theme: String) {
        dataStore.edit { it[KEY_READER_THEME] = theme }
    }

    suspend fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return
        dataStore.edit { prefs ->
            val current = prefs[KEY_RECENT_SEARCHES]?.split("||")?.filter { it.isNotBlank() } ?: emptyList()
            val updated = (listOf(trimmed) + current.filterNot { it.equals(trimmed, ignoreCase = true) }).take(8)
            prefs[KEY_RECENT_SEARCHES] = updated.joinToString("||")
        }
    }

    suspend fun clearRecentSearches() {
        dataStore.edit { it.remove(KEY_RECENT_SEARCHES) }
    }

    suspend fun setDefaultCollectionId(collectionId: Long?) {
        dataStore.edit { preferences ->
            if (collectionId != null) {
                preferences[KEY_DEFAULT_COLLECTION_ID] = collectionId
            } else {
                preferences.remove(KEY_DEFAULT_COLLECTION_ID)
            }
        }
    }

    suspend fun setCleanTrackingParams(enabled: Boolean) {
        dataStore.edit { it[KEY_CLEAN_TRACKING_PARAMS] = enabled }
    }

    suspend fun setAutoFetchMetadata(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_FETCH_METADATA] = enabled }
    }

    suspend fun setGithubToken(token: String?) {
        dataStore.edit { preferences ->
            if (!token.isNullOrBlank()) {
                preferences[KEY_GITHUB_TOKEN] = token.trim()
            } else {
                preferences.remove(KEY_GITHUB_TOKEN)
            }
        }
    }
}
