package com.desarrollodroide.network.model

import com.google.gson.annotations.SerializedName

data class BookmarkDTO (
    val id: Int?,
    val url: String?,
    val title: String?,
    val excerpt: String?,
    val author: String?,
    val public: Int?,
    val createdAt: String?,
    @SerializedName(value = "modified", alternate = ["modifiedAt"])
    val modified: String?,
    val imageURL: String?,
    val hasContent: Boolean?,
    val hasArchive: Boolean?,
    val hasEbook: Boolean?,
    val tags: List<TagDTO>?,
    @SerializedName("create_archive")
    val createArchive: Boolean?,
    @SerializedName("create_ebook")
    val createEbook: Boolean?,
)
