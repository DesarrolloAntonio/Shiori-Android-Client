package com.desarrollodroide.model

data class SyncBookmarksResponse(
    val deleted: List<Int>,
    val modified: ModifiedBookmarks
)