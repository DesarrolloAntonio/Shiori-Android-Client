package com.desarrollodroide.model

data class SyncBookmarksRequestPayload(
    val ids: List<Int>,
    val last_sync: Int,
    val page: Int
)