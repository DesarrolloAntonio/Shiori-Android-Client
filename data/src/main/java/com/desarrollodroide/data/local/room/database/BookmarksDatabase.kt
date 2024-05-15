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
import com.desarrollodroide.data.local.room.dao.TagDao
import com.desarrollodroide.data.local.room.entity.TagEntity

@Database(entities = [BookmarkEntity::class, TagEntity::class], version = 4)
@TypeConverters(TagsConverter::class)
abstract class BookmarksDatabase : RoomDatabase() {

    abstract fun bookmarksDao(): BookmarksDao
    abstract fun tagDao(): TagDao

    companion object {

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE bookmarks ADD COLUMN has_ebook INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE bookmarks ADD COLUMN create_ebook INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create table statement updated to include n_bookmarks
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS `tags` (
                `id` INTEGER PRIMARY KEY NOT NULL,
                `name` TEXT NOT NULL,
                `n_bookmarks` INTEGER NOT NULL
            )
        """
                )
            }
        }

        fun create(context: Context): BookmarksDatabase {
            return Room.databaseBuilder(
                context,
                BookmarksDatabase::class.java, "bookmarks_database"
            )
                .allowMainThreadQueries()
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
        }
    }
}