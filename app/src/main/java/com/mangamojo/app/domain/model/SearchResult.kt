package com.mangamojo.app.domain.model

/** A page of search/browse results plus the paging cursor metadata. */
data class SearchResult(
    val items: List<Manga>,
    val total: Int,
    val offset: Int,
    val limit: Int,
) {
    val hasMore: Boolean get() = offset + items.size < total
    val nextOffset: Int get() = offset + limit
}
