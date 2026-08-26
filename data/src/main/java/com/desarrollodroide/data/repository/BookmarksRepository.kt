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

  /**
   * Re-fetches one bookmark rather than walking every page of the server.
   *
   * The list endpoint takes a keyword, so asking for the bookmark's own url costs a single
   * request. A full sync for one card meant a request per thirty bookmarks in the library.
   *
   * Returns null when the server does not know that url.
   */
  suspend fun refreshBookmark(
    xSession: String,
    serverUrl: String,
    bookmark: Bookmark
  ): Bookmark?

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

  /**
   * Sets the tags on the given bookmarks. This replaces what they had, it does not merge, so the
   * caller has to send the full set it wants each bookmark to end up with.
   */
  suspend fun addTagsToBookmarks(
    token: String,
    serverUrl: String,
    bookmarkIds: List<Int>,
    tagIds: List<Int>,
  ): List<Bookmark>

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