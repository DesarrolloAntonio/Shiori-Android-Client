package com.shiori.domain.model

data class Bookmark (
    val id: Int,
    val url: String,
    val title: String,
    val excerpt: String,
    val author: String,
    val public: Int,
    val modified: String,
    val imageURL: String,
    val hasContent: Boolean,
    val hasArchive: Boolean,
    val tags: List<Tag>,
    val createArchive: Boolean
)
