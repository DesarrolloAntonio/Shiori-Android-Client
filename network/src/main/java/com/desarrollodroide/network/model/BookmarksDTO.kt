package com.desarrollodroide.network.model

data class BookmarksDTO (
    val ok: Boolean? = null,
    val message: BookmarksMessageDTO? = null,
    val maxPage: Int? = null,
    val page: Int? = null,
    val bookmarks: List<BookmarkDTO>? = null,
) {
    /** Resolves bookmarks from either v1.8+ (wrapped in message) or legacy format */
    fun resolvedBookmarks(): List<BookmarkDTO>? = bookmarks ?: message?.bookmarks
    fun resolvedPage(): Int? = page ?: message?.page
    fun resolvedMaxPage(): Int? = maxPage ?: message?.maxPage
}

data class BookmarksMessageDTO(
    val bookmarks: List<BookmarkDTO>?,
    val maxPage: Int?,
    val page: Int?,
)
