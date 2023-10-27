package com.desarrollodroide.data.repository

import kotlinx.coroutines.flow.Flow
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.common.result.Result

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