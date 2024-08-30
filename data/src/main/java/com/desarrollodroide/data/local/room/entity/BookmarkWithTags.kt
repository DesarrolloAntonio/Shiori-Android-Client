package com.desarrollodroide.data.local.room.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BookmarkWithTags(
    @Embedded val bookmark: BookmarkEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(BookmarkTagCrossRef::class)
    )
    val tags: List<TagEntity>
)
