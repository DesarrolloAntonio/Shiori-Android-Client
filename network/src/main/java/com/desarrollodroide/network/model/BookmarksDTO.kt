package com.desarrollodroide.network.model

data class BookmarksDTO (
    val maxPage: Int?,
    val page: Int?,
    val bookmarks: List<BookmarkDTO>?,
)
