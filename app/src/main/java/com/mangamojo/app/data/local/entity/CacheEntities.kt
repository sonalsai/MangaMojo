package com.mangamojo.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cached manga metadata. [cachedAt] drives freshness checks and background
 * eviction. [sourceId] is carried on every row so a future multi-provider
 * build can disambiguate ids across sources.
 */
@Entity(tableName = "cached_manga")
data class CachedMangaEntity(
    @PrimaryKey val mangaId: String,
    val sourceId: String,
    val title: String,
    val coverUrl: String?,
    val status: String,
    val contentRating: String,
    val year: Int?,
    val description: String,
    val altTitles: List<String>,
    val authors: List<String>,
    val artists: List<String>,
    val tags: List<String>,
    val availableLanguages: List<String>,
    val cachedAt: Long,
)

/** Cached chapter feed. [orderIndex] preserves the provider's feed ordering. */
@Entity(
    tableName = "cached_chapters",
    indices = [Index("mangaId")],
)
data class CachedChapterEntity(
    @PrimaryKey val chapterId: String,
    val mangaId: String,
    val sourceId: String,
    val volume: String?,
    val chapter: String?,
    val title: String,
    val pages: Int,
    val translatedLanguage: String?,
    val scanlationGroup: String,
    val publishAt: String?,
    val externalUrl: String?,
    val label: String,
    val orderIndex: Int,
    val cachedAt: Long,
)
