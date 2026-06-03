package com.mangamojo.app.domain.model

/**
 * A readable chapter. [externalUrl] is set when the chapter is hosted off-site
 * (e.g. official MangaPlus releases); such chapters open in a browser rather
 * than the in-app reader.
 */
data class Chapter(
    val id: String,
    val sourceId: String,
    val mangaId: String,
    val volume: String?,
    val chapter: String?,
    val title: String,
    val pages: Int,
    val translatedLanguage: String?,
    val scanlationGroup: String,
    val publishAt: String?,
    val externalUrl: String?,
    val label: String,
) {
    val isExternal: Boolean get() = !externalUrl.isNullOrBlank()
}
