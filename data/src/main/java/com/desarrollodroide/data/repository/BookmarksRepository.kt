package com.desarrollodroide.data.repository

import kotlinx.coroutines.flow.Flow
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.model.UpdateCachePayload
import com.desarrollodroide.network.model.UpdateCachePayloadDTO

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

  suspend fun deleteAllLocalBookmarks()
  fun updateBookmarkCache(
    xSession: String,
    serverUrl: String,
    updateCachePayload: UpdateCachePayload
  ): Flow<Result<Bookmark>>

  fun updateBookmarkCacheV1(
    token: String,
    serverUrl: String,
    updateCachePayload: UpdateCachePayload
  ): Flow<Result<Bookmark>>
}