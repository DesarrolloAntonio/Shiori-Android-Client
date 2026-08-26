package com.desarrollodroide.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.desarrollodroide.data.local.room.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTags(tags: List<TagEntity>)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()

    /**
     * Replaces the cached tags in one transaction. As two separate calls, a failure between them
     * left the user with no tags at all.
     */
    @Transaction
    suspend fun replaceAllTags(tags: List<TagEntity>) {
        deleteAllTags()
        insertAllTags(tags)
    }

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTagById(tagId: Int)

    @Query("UPDATE tags SET name = :name WHERE id = :tagId")
    suspend fun renameTag(tagId: Int, name: String)

    @Transaction
    @Query("""
        SELECT DISTINCT t.* 
        FROM tags t
        LEFT JOIN bookmark_tag_cross_ref bt ON t.id = bt.tagId 
        ORDER BY t.name
    """)
    fun observeAllTags(): Flow<List<TagEntity>>
}
