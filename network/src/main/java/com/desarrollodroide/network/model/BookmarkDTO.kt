package com.desarrollodroide.network.model

data class BookmarkDTO (
    val id: Int?,
    val url: String?,
    val title: String?,
    val excerpt: String?,
    val author: String?,
    val public: Int?,
    val modified: String?,
    val imageURL: String?,
    val hasContent: Boolean?,
    val hasArchive: Boolean?,
    val tags: List<TagDTO>?,
    val createArchive: Boolean?
)
