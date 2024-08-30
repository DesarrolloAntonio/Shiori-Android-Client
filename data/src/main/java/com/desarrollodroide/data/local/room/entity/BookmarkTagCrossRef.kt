package com.desarrollodroide.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "bookmark_tag_cross_ref", primaryKeys = ["bookmarkId", "tagId"])
data class BookmarkTagCrossRef(
    @ColumnInfo(name = "bookmarkId") val bookmarkId: Int,
    @ColumnInfo(name = "tagId") val tagId: Int
)