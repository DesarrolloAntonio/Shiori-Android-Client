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
    @SerializedName(value = "create_archive", alternate = ["createArchive"])
    val createArchive: Boolean?,
    @SerializedName(value = "create_ebook", alternate = ["createEbook"])
    val createEbook: Boolean?,
)
