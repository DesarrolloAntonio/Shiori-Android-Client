package com.desarrollodroide.network.model

data class ModifiedBookmarksDTO(
    val bookmarks: List<BookmarkDTO>?,
    val maxPage: Int?,
    val page: Int?
)