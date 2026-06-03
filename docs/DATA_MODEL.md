# Data Model

All models are **source-agnostic**. Every model and persisted row carries a `sourceId`
(always `"mangadex"` in Phase 1) so the same types can hold data from multiple providers
later without schema changes.

## Domain models (`domain.model`)

| Model | Key fields |
| --- | --- |
| `Manga` | `id, sourceId, title, coverUrl, status, contentRating, year` — list/grid summary |
| `MangaDetails` | summary + `altTitles, description, authors, artists, tags, availableLanguages`; `toManga()` collapses to a summary |
| `Chapter` | `id, sourceId, mangaId, volume, chapter, title, pages, translatedLanguage, scanlationGroup, publishAt, externalUrl, label`; `isExternal` |
| `Page` | `index, imageUrl` |
| `SearchResult` | `items, total, offset, limit`; `hasMore`, `nextOffset` |
| `SearchQuery` | `title, offset, limit, sort (SearchSort), contentRatings, includedTagIds` |
| `ReadingProgress` | `mangaId, chapterId, page, total, completed, updatedAt` |
| `Favorite` | `mangaId, sourceId, title, coverUrl, status, addedAt` |
| `HistoryEntry` | `mangaId, sourceId, title, coverUrl, chapterId, chapterLabel, page, total, readAt`; `isCompleted`, `progressFraction` |
| `AppSettings` | `themeMode, readingDirection, dataSaver, contentRatings, translatedLanguage` |

**Enums:** `MangaStatus` (ONGOING/COMPLETED/HIATUS/CANCELLED/UNKNOWN, with `from(raw)`),
`ThemeMode` (SYSTEM/LIGHT/DARK), `ReadingDirection` (VERTICAL/LTR/RTL),
`SearchSort` (RELEVANCE/POPULAR/LATEST/NEWEST/RATING — each maps to MangaDex `order[...]`).

## Room schema (`mangamojo.db`, version 1)

| Table | Entity | Primary key | Notes |
| --- | --- | --- | --- |
| `cached_manga` | `CachedMangaEntity` | `mangaId` | full details + `cachedAt`; list columns stored as JSON via `Converters` |
| `cached_chapters` | `CachedChapterEntity` | `chapterId` | indexed by `mangaId`; `orderIndex` preserves feed order; `cachedAt` |
| `favorites` | `FavoriteEntity` | `mangaId` | ordered by `addedAt` |
| `history` | `HistoryEntity` | `mangaId` | one row per manga (most recent chapter) |
| `reading_progress` | `ReadingProgressEntity` | `chapterId` | indexed by `mangaId`; `completed` flag |
| `bookmarks` | `BookmarkEntity` | auto `id` | indexed by `mangaId`/`chapterId`; schema ready, minimal UI in v1.0.0 |

DAOs return `Flow` for everything reactive (favorites, history, progress, cached count).
`@Upsert` is used for inserts/updates; `Converters` serializes `List<String>` columns to
JSON with kotlinx-serialization.

Migrations: v1.0.0 ships with `fallbackToDestructiveMigration(dropAllTables = true)` (no
migrations needed yet). Real migrations will be added when the schema changes.

## Settings (DataStore Preferences — `mangamojo_settings`)

| Key | Type | Default |
| --- | --- | --- |
| `theme_mode` | String (`ThemeMode`) | `SYSTEM` |
| `reading_direction` | String (`ReadingDirection`) | `VERTICAL` |
| `data_saver` | Boolean | `false` |
| `content_ratings` | String set | `["safe", "suggestive"]` |
| `translated_language` | String | `en` |

## MangaDex endpoints used

| Purpose | Endpoint |
| --- | --- |
| Search / browse | `GET /manga?title=&limit=&offset=&contentRating[]=&includes[]=cover_art,author,artist&order[...]=` |
| Details | `GET /manga/{id}?includes[]=cover_art,author,artist` |
| Chapter feed | `GET /manga/{id}/feed?translatedLanguage[]=&order[volume]=desc&order[chapter]=desc&includes[]=scanlation_group` |
| Page delivery | `GET /at-home/server/{chapterId}` → `baseUrl + /data|/data-saver + /{hash}/{file}` |
| Covers | `https://uploads.mangadex.org/covers/{mangaId}/{fileName}.512.jpg` |
