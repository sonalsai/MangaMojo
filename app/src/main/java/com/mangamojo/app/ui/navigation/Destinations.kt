package com.mangamojo.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** Central route table. Detail/reader routes expose typed builders. */
object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val FAVORITES = "favorites"
    const val HISTORY = "history"
    const val SETTINGS = "settings"

    const val ARG_MANGA_ID = "mangaId"
    const val ARG_CHAPTER_ID = "chapterId"

    const val DETAILS = "details/{$ARG_MANGA_ID}"
    const val READER = "reader/{$ARG_MANGA_ID}/{$ARG_CHAPTER_ID}"

    fun details(mangaId: String): String = "details/$mangaId"
    fun reader(mangaId: String, chapterId: String): String = "reader/$mangaId/$chapterId"
}

/** Tabs shown in the side navigation drawer. */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    HOME(Routes.HOME, "Home", Icons.Rounded.Home),
    SEARCH(Routes.SEARCH, "Search", Icons.Rounded.Search),
    FAVORITES(Routes.FAVORITES, "Library", Icons.Rounded.Favorite),
    HISTORY(Routes.HISTORY, "History", Icons.Rounded.History),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Rounded.Settings),
}
