package com.mangamojo.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mangamojo.app.reader.ReaderScreen
import com.mangamojo.app.ui.details.DetailsScreen
import com.mangamojo.app.ui.favorites.FavoritesScreen
import com.mangamojo.app.ui.history.HistoryScreen
import com.mangamojo.app.ui.home.HomeScreen
import com.mangamojo.app.ui.search.SearchScreen
import com.mangamojo.app.ui.settings.SettingsScreen

@Composable
fun MangaMojoNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TopLevelDestination.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = { navController.navigateToTab(destination.route) },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onMangaClick = { navController.navigate(Routes.details(it)) },
                    onSeeFavorites = { navController.navigateToTab(Routes.FAVORITES) },
                )
            }

            composable(Routes.SEARCH) {
                SearchScreen(onMangaClick = { navController.navigate(Routes.details(it)) })
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(onMangaClick = { navController.navigate(Routes.details(it)) })
            }

            composable(Routes.HISTORY) {
                HistoryScreen(onResume = { mangaId, chapterId ->
                    navController.navigate(Routes.reader(mangaId, chapterId))
                })
            }

            composable(Routes.SETTINGS) { SettingsScreen() }

            composable(
                route = Routes.DETAILS,
                arguments = listOf(navArgument(Routes.ARG_MANGA_ID) { type = NavType.StringType }),
            ) { entry ->
                val mangaId = entry.arguments?.getString(Routes.ARG_MANGA_ID).orEmpty()
                DetailsScreen(
                    onBack = { navController.popBackStack() },
                    onChapterClick = { chapterId ->
                        navController.navigate(Routes.reader(mangaId, chapterId))
                    },
                )
            }

            composable(
                route = Routes.READER,
                arguments = listOf(
                    navArgument(Routes.ARG_MANGA_ID) { type = NavType.StringType },
                    navArgument(Routes.ARG_CHAPTER_ID) { type = NavType.StringType },
                ),
            ) {
                ReaderScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

/** Switch bottom-nav tabs while preserving each tab's state. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
