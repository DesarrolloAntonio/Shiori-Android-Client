package com.desarrollodroide.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.model.ReadableContent
import com.desarrollodroide.model.SyncBookmarksRequestPayload
import com.desarrollodroide.model.SyncBookmarksResponse
import com.desarrollodroide.model.Tag
import com.desarrollodroide.model.UpdateCachePayload

interface BookmarksRepository {

  fun getBookmarks(
    xSession: String,
    serverUrl: String
  ): Flow<Result<List<Bookmark>?>>

  fun getPagingBookmarks(
      xSession: String,
      serverUrl: String,
      searchText: String,
      tags: List<Tag>,
      saveToLocal: Boolean
  ): Flow<PagingData<Bookmark>>

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

  fun syncBookmarks(
    token: String,
    serverUrl: String,
    syncBookmarksRequestPayload: SyncBookmarksRequestPayload
  ): Flow<Result<SyncBookmarksResponse>>
}