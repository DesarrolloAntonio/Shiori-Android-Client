package com.desarrollodroide.network.model

data class SyncBookmarksResponseDTO(
    val deleted: List<Int>?,
    val message: SyncBookmarksMessageDTO
)