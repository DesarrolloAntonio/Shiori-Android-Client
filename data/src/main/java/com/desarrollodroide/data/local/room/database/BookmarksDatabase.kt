package com.desarrollodroide.data.local.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.local.room.converters.TagsConverter

@Database(entities = [BookmarkEntity::class], version = 1)
@TypeConverters(TagsConverter::class)
abstract class BookmarksDatabase : RoomDatabase() {

    abstract fun bookmarksDao(): BookmarksDao

    companion object {

        fun create(context: Context): BookmarksDatabase {

            return Room.databaseBuilder(
                context,
                BookmarksDatabase::class.java, "bookmarks_database"
            )
                .allowMainThreadQueries()
                .build()
        }
    }

}