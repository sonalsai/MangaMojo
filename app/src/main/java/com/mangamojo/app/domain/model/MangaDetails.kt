package com.mangamojo.app.domain.model

/** Full manga metadata for the details screen. */
data class MangaDetails(
    val id: String,
    val sourceId: String,
    val title: String,
    val altTitles: List<String>,
    val description: String,
    val coverUrl: String?,
    val status: MangaStatus,
    val contentRating: String,
    val year: Int?,
    val authors: List<String>,
    val artists: List<String>,
    val tags: List<String>,
    val availableLanguages: List<String>,
) {
    /** Collapse to a summary for caching / library use. */
    fun toManga(): Manga = Manga(
        id = id,
        sourceId = sourceId,
        title = title,
        coverUrl = coverUrl,
        status = status,
        contentRating = contentRating,
        year = year,
    )
}
