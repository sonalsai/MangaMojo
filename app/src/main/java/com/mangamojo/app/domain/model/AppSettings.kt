package com.mangamojo.app.domain.model

import com.mangamojo.app.core.MangaDex

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Reading direction. Phase 1 ships VERTICAL (webtoon-style); LTR/RTL are
 * placeholders wired through Settings for Phase 2 paged modes.
 */
enum class ReadingDirection { VERTICAL, LTR, RTL }

/** All user preferences, surfaced as a single immutable snapshot. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val readingDirection: ReadingDirection = ReadingDirection.VERTICAL,
    val dataSaver: Boolean = false,
    val contentRatings: Set<String> = MangaDex.DEFAULT_CONTENT_RATINGS.toSet(),
    val translatedLanguage: String = MangaDex.DEFAULT_LANGUAGE,
)
