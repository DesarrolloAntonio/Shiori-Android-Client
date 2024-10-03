package com.desarrollodroide.model

data class ModifiedBookmarks(
    val bookmarks: List<Bookmark>,
    val maxPage: Int,
    val page: Int
)