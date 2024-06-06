package com.desarrollodroide.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmark_html")
data class BookmarkHtmlEntity(
    @PrimaryKey
    val id: Int,
    val url: String,
    val readableContentHtml: String
)
