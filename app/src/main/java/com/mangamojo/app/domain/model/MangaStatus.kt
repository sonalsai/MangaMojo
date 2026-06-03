package com.mangamojo.app.domain.model

/** Publication status, normalized away from any one source's vocabulary. */
enum class MangaStatus(val raw: String, val label: String) {
    ONGOING("ongoing", "Ongoing"),
    COMPLETED("completed", "Completed"),
    HIATUS("hiatus", "Hiatus"),
    CANCELLED("cancelled", "Cancelled"),
    UNKNOWN("unknown", "Unknown");

    companion object {
        fun from(raw: String?): MangaStatus =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: UNKNOWN
    }
}
