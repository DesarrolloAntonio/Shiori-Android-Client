package com.desarrollodroide.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.desarrollodroide.model.Tag

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val id: Int,
    val url: String,
    val title: String,
    val excerpt: String,
    val author: String,
    @ColumnInfo(name = "is_public")
    val isPublic: Int,
    @ColumnInfo(name = "modified_date")
    val modified: String,
    @ColumnInfo(name = "image_url")
    val imageURL: String,
    @ColumnInfo(name = "has_content")
    val hasContent: Boolean,
    @ColumnInfo(name = "has_archive")
    val hasArchive: Boolean,
    @ColumnInfo(name = "has_ebook")
    val hasEbook: Boolean,
    val tags: List<Tag>,
    @ColumnInfo(name = "create_archive")
    val createArchive: Boolean,
    @ColumnInfo(name = "create_ebook")
    val createEbook: Boolean,
)