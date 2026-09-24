package com.damianrdev.save.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String? = null,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    data object Home : Screen(
        route = "home",
        title = "Inicio",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Library : Screen(
        route = "library",
        title = "Biblioteca",
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder
    )

    data object Collections : Screen(
        route = "collections",
        title = "Colecciones",
        selectedIcon = Icons.Filled.Folder,
        unselectedIcon = Icons.Outlined.Folder
    )

    data object Search : Screen(
        route = "search",
        title = "Buscar",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    )

    data object Settings : Screen(
        route = "settings",
        title = "Ajustes",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    data object Detail : Screen(route = "detail/{bookmarkId}") {
        fun createRoute(bookmarkId: Long) = "detail/$bookmarkId"
    }

    data object Reader : Screen(route = "reader/{bookmarkId}") {
        fun createRoute(bookmarkId: Long) = "reader/$bookmarkId"
    }

    data object Edit : Screen(route = "edit/{bookmarkId}") {
        fun createRoute(bookmarkId: Long) = "edit/$bookmarkId"
    }

    data object Trash : Screen(route = "trash", title = "Papelera")

    data object ImportExport : Screen(route = "import_export", title = "Importar / Exportar")

    data object CollectionDetail : Screen(route = "collection/{collectionId}") {
        fun createRoute(collectionId: Long) = "collection/$collectionId"
    }
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Library,
    Screen.Collections,
    Screen.Search,
    Screen.Settings
)
