package com.shiori.data.repository

import kotlinx.coroutines.flow.Flow
import com.shiori.model.Bookmark
import com.shiori.common.result.Result
import com.shiori.network.model.BookmarkDTO

interface BookmarksRepository {

  fun getBookmarks(
    xSession: String,
    serverUrl: String
  ): Flow<Result<List<Bookmark>?>>

  fun addBookmark(
    xSession: String,
    serverUrl: String,
    bookmark: Bookmark
  ): Flow<Result<Bookmark>>


  fun deleteBookmark(
    xSession: String,
    serverUrl: String,
    bookmarkId: Int
  ): Flow<Result<Unit>>

  fun editBookmark(
    xSession: String,
    serverUrl: String,
    bookmark: Bookmark
  ): Flow<Result<Bookmark>>
}