package com.shiori.data.di

import com.shiori.data.local.room.database.BookmarksDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


fun databaseModule() = module {

  single { BookmarksDatabase.create(androidContext()) }
  single { get<BookmarksDatabase>().bookmarksDao() }

}

