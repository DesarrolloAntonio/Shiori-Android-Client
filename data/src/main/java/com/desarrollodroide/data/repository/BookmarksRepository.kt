package com.desarrollodroide.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.model.ReadableContent
import com.desarrollodroide.model.Tag
import com.desarrollodroide.model.UpdateCachePayload

interface BookmarksRepository {

  fun getBookmarks(
    xSession: String,
    serverUrl: String
  ): Flow<Result<List<Bookmark>?>>

  suspend fun addBookmark(
    xSession: String,
    serverUrl: String,
    bookmark: Bookmark
  ): Bookmark

  suspend fun deleteBookmark(
    xSession: String,
    serverUrl: String,
    bookmarkId: Int
  )

  suspend fun editBookmark(
    xSession: String,
    serverUrl: String,
    bookmark: Bookmark
  ): Bookmark

  suspend fun deleteAllLocalBookmarks()

  suspend fun updateBookmarkCacheV1(
    token: String,
    serverUrl: String,
    updateCachePayload: UpdateCachePayload,
    bookmark: Bookmark?,
  ): List<Bookmark>

  fun getBookmarkReadableContent(
    token: String,
    serverUrl: String,
    bookmarkId: Int
  ): Flow<Result<ReadableContent>>

  suspend fun syncAllBookmarks(
    xSession: String,
    serverUrl: String
  ): Flow<SyncStatus>

  fun getLocalPagingBookmarks(
    tags: List<Tag>,
    searchText: String
  ): Flow<PagingData<Bookmark>>

}