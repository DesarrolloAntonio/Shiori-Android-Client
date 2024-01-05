package com.desarrollodroide.data.di

import com.desarrollodroide.data.local.room.database.BookmarksDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun databaseModule() = module {

  single { BookmarksDatabase.create(androidContext()) }
  single { get<BookmarksDatabase>().bookmarksDao() }

}

