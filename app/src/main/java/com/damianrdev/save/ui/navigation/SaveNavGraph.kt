package com.damianrdev.save.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.damianrdev.save.ui.collections.CollectionsScreen
import com.damianrdev.save.ui.collections.CollectionsViewModel
import com.damianrdev.save.ui.detail.BookmarkDetailScreen
import com.damianrdev.save.ui.detail.BookmarkDetailViewModel
import com.damianrdev.save.ui.edit.EditBookmarkScreen
import com.damianrdev.save.ui.edit.EditBookmarkViewModel
import com.damianrdev.save.ui.home.HomeScreen
import com.damianrdev.save.ui.home.HomeViewModel
import com.damianrdev.save.ui.importexport.ImportExportScreen
import com.damianrdev.save.ui.importexport.ImportExportViewModel
import com.damianrdev.save.ui.search.SearchScreen
import com.damianrdev.save.ui.search.SearchViewModel
import com.damianrdev.save.ui.settings.SettingsScreen
import com.damianrdev.save.ui.settings.SettingsViewModel
import com.damianrdev.save.ui.trash.TrashArchivedScreen
import com.damianrdev.save.ui.trash.TrashArchivedViewModel

@Composable
fun SaveApp(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    val isTopLevelDestination = BottomNavItems.any { it.route == currentDestination }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (isTopLevelDestination) {
                androidx.compose.material3.Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                ) {
                    NavigationBar(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        BottomNavItems.forEach { screen ->
                            val isSelected = currentDestination == screen.route
                            val icon = if (isSelected) screen.selectedIcon else screen.unselectedIcon

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    icon?.let {
                                        Icon(imageVector = it, contentDescription = screen.title)
                                    }
                                },
                                label = {
                                    Text(
                                        text = screen.title ?: "",
                                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                route = Screen.Home.route,
                deepLinks = listOf(
                    navDeepLink { uriPattern = "save://home" },
                    navDeepLink { uriPattern = "save://favorites" }
                )
            ) { backStackEntry ->
                val homeViewModel: HomeViewModel = hiltViewModel()
                val uri = backStackEntry.arguments?.getString(androidx.navigation.NavController.KEY_DEEP_LINK_INTENT)
                if (uri?.contains("favorites") == true) {
                    homeViewModel.setFilter(com.damianrdev.save.ui.home.HomeFilter.FAVORITES)
                }
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToDetail = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    }
                )
            }

            composable(Screen.Library.route) {
                val libraryViewModel: HomeViewModel = hiltViewModel()
                com.damianrdev.save.ui.library.LibraryScreen(
                    viewModel = libraryViewModel,
                    onNavigateToDetail = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    }
                )
            }

            composable(Screen.Collections.route) {
                val collectionsViewModel: CollectionsViewModel = hiltViewModel()
                CollectionsScreen(
                    viewModel = collectionsViewModel,
                    onNavigateToCollectionDetail = { colId ->
                        navController.navigate(Screen.CollectionDetail.createRoute(colId))
                    }
                )
            }

            composable(
                route = Screen.Search.route,
                deepLinks = listOf(navDeepLink { uriPattern = "save://search" })
            ) {
                val searchViewModel: SearchViewModel = hiltViewModel()
                SearchScreen(
                    viewModel = searchViewModel,
                    onNavigateToDetail = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    }
                )
            }

            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToTrash = { navController.navigate(Screen.Trash.route) },
                    onNavigateToImportExport = { navController.navigate(Screen.ImportExport.route) }
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("bookmarkId") { type = NavType.LongType })
            ) {
                val detailViewModel: BookmarkDetailViewModel = hiltViewModel()
                BookmarkDetailScreen(
                    viewModel = detailViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id ->
                        navController.navigate(Screen.Edit.createRoute(id))
                    },
                    onNavigateToReader = { id ->
                        navController.navigate(Screen.Reader.createRoute(id))
                    }
                )
            }

            composable(
                route = Screen.Reader.route,
                arguments = listOf(navArgument("bookmarkId") { type = NavType.LongType })
            ) {
                val detailViewModel: BookmarkDetailViewModel = hiltViewModel()
                com.damianrdev.save.ui.reader.ReaderScreen(
                    viewModel = detailViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Edit.route,
                arguments = listOf(navArgument("bookmarkId") { type = NavType.LongType })
            ) {
                val editViewModel: EditBookmarkViewModel = hiltViewModel()
                EditBookmarkScreen(
                    viewModel = editViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Trash.route) {
                val trashViewModel: TrashArchivedViewModel = hiltViewModel()
                TrashArchivedScreen(
                    viewModel = trashViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ImportExport.route) {
                val importExportViewModel: ImportExportViewModel = hiltViewModel()
                ImportExportScreen(
                    viewModel = importExportViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CollectionDetail.route,
                arguments = listOf(navArgument("collectionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val colId = backStackEntry.arguments?.getLong("collectionId")
                val homeViewModel: HomeViewModel = hiltViewModel()
                homeViewModel.selectCollection(colId)
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToDetail = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    }
                )
            }
        }
    }
}
