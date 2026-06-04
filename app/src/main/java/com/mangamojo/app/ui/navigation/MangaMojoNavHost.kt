package com.mangamojo.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.launch

@Composable
fun MangaMojoNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showDrawer = TopLevelDestination.entries.any { it.route == currentRoute }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openDrawer: () -> Unit = {
        scope.launch { drawerState.open() }
        Unit
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showDrawer,
        drawerContent = {
            MangaMojoDrawer(
                currentRoute = currentRoute,
                onDestinationClick = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigateToTab(route)
                },
            )
        },
    ) {
        Scaffold { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        onMangaClick = { navController.navigate(Routes.details(it)) },
                        onResume = { mangaId, chapterId ->
                            navController.navigate(Routes.reader(mangaId, chapterId))
                        },
                        onSearch = { navController.navigateToTab(Routes.SEARCH) },
                        onSeeFavorites = { navController.navigateToTab(Routes.FAVORITES) },
                        onSeeHistory = { navController.navigateToTab(Routes.HISTORY) },
                        onOpenDrawer = openDrawer,
                    )
                }

                composable(Routes.SEARCH) {
                    SearchScreen(
                        onMangaClick = { navController.navigate(Routes.details(it)) },
                        onOpenDrawer = openDrawer,
                    )
                }

                composable(Routes.FAVORITES) {
                    FavoritesScreen(
                        onMangaClick = { navController.navigate(Routes.details(it)) },
                        onOpenDrawer = openDrawer,
                    )
                }

                composable(Routes.HISTORY) {
                    HistoryScreen(
                        onResume = { mangaId, chapterId ->
                            navController.navigate(Routes.reader(mangaId, chapterId))
                        },
                        onOpenDrawer = openDrawer,
                    )
                }

                composable(Routes.SETTINGS) {
                    SettingsScreen(onOpenDrawer = openDrawer)
                }

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
}

@Composable
private fun MangaMojoDrawer(
    currentRoute: String?,
    onDestinationClick: (String) -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 20.dp)) {
            Text(
                text = "MANGAMOJO",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Text(
                text = "Shonen Crimson",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f))
            Spacer(Modifier.height(8.dp))

            TopLevelDestination.entries.forEach { destination ->
                NavigationDrawerItem(
                    selected = currentRoute == destination.route,
                    onClick = { onDestinationClick(destination.route) },
                    icon = { Icon(destination.icon, contentDescription = null) },
                    label = { Text(destination.label, fontWeight = FontWeight.Bold) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }
    }
}

/** Switch drawer tabs while preserving each tab's state. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
