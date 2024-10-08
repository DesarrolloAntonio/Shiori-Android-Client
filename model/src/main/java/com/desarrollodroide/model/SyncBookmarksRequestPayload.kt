package com.desarrollodroide.model

data class SyncBookmarksRequestPayload(
    val ids: List<Int>,
    val last_sync: Long,
    val page: Int
)