package com.desarrollodroide.domain.usecase

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.model.SyncBookmarksRequestPayload
import com.desarrollodroide.model.SyncBookmarksResponse
import kotlinx.coroutines.flow.Flow
import com.desarrollodroide.common.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import com.desarrollodroide.data.mapper.toEntityModel
import java.time.ZoneId
import java.time.ZonedDateTime

class SyncBookmarksUseCase(
    private val bookmarksRepository: BookmarksRepository,
    private val bookmarkDatabase: BookmarksDao,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    ) {
    operator fun invoke(
        token: String,
        serverUrl: String,
        syncBookmarksRequestPayload: SyncBookmarksRequestPayload
    ): Flow<Result<SyncBookmarksResponse>> {
        return bookmarksRepository.syncBookmarks(
            token = token,
            serverUrl = serverUrl,
            syncBookmarksRequestPayload = syncBookmarksRequestPayload
        ).flowOn(Dispatchers.IO)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun handleSuccessfulSync(
        syncResponse: SyncBookmarksResponse,
        currentLastSync: Long
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Handle deleted bookmarks
                syncResponse.deleted.forEach { id ->
                    bookmarkDatabase.deleteBookmarkById(id)
                }

                // Handle new and modified bookmarks
                val bookmarkEntities = syncResponse.modified.bookmarks.map { remoteBookmark ->
                    val localBookmark = bookmarkDatabase.getBookmarkById(remoteBookmark.id)
                    Log.d("TAG", "Processing bookmark ID: ${remoteBookmark.id}, Local Modified: ${localBookmark?.modified}, Remote Modified: ${remoteBookmark.modified}")
                    remoteBookmark.toEntityModel()
                }

                if (bookmarkEntities.isNotEmpty()) {
                    bookmarkEntities.forEach { bookmark ->
                        val existingBookmark = bookmarkDatabase.getBookmarkById(bookmark.id)
                        if (existingBookmark == null) {
                            // New bookmark, insert it
                            bookmarkDatabase.insertBookmark(bookmark)
                        } else {
                            // Existing bookmark, update it
                            bookmarkDatabase.updateBookmarkWithTags(bookmark)
                        }
                    }
                }

                // Check if there are more pages to sync
                val currentPage = syncResponse.modified.page
                val maxPage = syncResponse.modified.maxPage

                if (currentPage < maxPage) {
                    // Get updated list of all local bookmark IDs for the next page
                    val updatedLocalBookmarkIds = bookmarkDatabase.getAllBookmarkIds()
                    invoke(
                        token = settingsPreferenceDataSource.getToken(),
                        serverUrl = settingsPreferenceDataSource.getUrl(),
                        syncBookmarksRequestPayload = SyncBookmarksRequestPayload(
                            ids = updatedLocalBookmarkIds,
                            last_sync = currentLastSync,
                            page = currentPage + 1
                        )
                    ).collect { result ->
                        if (result is Result.Success) {
                            result.data?.let {
                                handleSuccessfulSync(it, currentLastSync)
                            }
                        }
                    }
                } else {
                    // Sync is complete, update last sync timestamp
                    val newLastSync = ZonedDateTime.now(ZoneId.systemDefault()).toEpochSecond() // Convert to seconds
                    settingsPreferenceDataSource.setLastSyncTimestamp(newLastSync)
                }
            } catch (e: Exception) {
                Log.e("SyncBookmarksUseCase", "Error handling sync response: ${e.message}")
            }
        }
    }
}