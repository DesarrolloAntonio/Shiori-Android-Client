package com.desarrollodroide.data.local.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.local.room.converters.TagsConverter

@Database(entities = [BookmarkEntity::class], version = 2)
@TypeConverters(TagsConverter::class)
abstract class BookmarksDatabase : RoomDatabase() {

    abstract fun bookmarksDao(): BookmarksDao



    companion object {

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE bookmarks ADD COLUMN has_ebook INTEGER NOT NULL DEFAULT 0")
            }
        }
        fun create(context: Context): BookmarksDatabase {

            return Room.databaseBuilder(
                context,
                BookmarksDatabase::class.java, "bookmarks_database"
            )
                .allowMainThreadQueries()
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }

}