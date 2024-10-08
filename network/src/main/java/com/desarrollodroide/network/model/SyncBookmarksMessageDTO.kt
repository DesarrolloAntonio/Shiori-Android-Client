package com.desarrollodroide.network.model

data class SyncBookmarksMessageDTO(
    val deleted: List<Int>?,
    val modified: ModifiedBookmarksDTO?
)