package com.desarrollodroide.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.data.extensions.toJson
import com.desarrollodroide.data.helpers.SESSION_HAS_BEEN_EXPIRED
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.mapper.*
import com.desarrollodroide.data.repository.paging.BookmarkPagingSource
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.ReadableContent
import com.desarrollodroide.model.SyncBookmarksRequestPayload
import com.desarrollodroide.model.SyncBookmarksResponse
import com.desarrollodroide.model.Tag
import com.desarrollodroide.model.UpdateCachePayload
import com.desarrollodroide.network.model.BookmarkDTO
import com.desarrollodroide.network.model.BookmarksDTO
import com.desarrollodroide.network.model.ReadableContentResponseDTO
import com.desarrollodroide.network.model.SyncBookmarksResponseDTO
import com.desarrollodroide.network.retrofit.NetworkBoundResource
import com.desarrollodroide.network.retrofit.NetworkNoCacheResource
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BookmarksRepositoryImpl(
    private val apiService: RetrofitNetwork,
    private val bookmarksDao: BookmarksDao,
    private val errorHandler: ErrorHandler
) : BookmarksRepository {

    private val TAG = "BookmarksRepository"

    override fun getBookmarks(
        xSession: String,
        serverUrl: String
    ) = object :
        NetworkBoundResource<BookmarksDTO, List<Bookmark>>(errorHandler = errorHandler) {

        override suspend fun saveRemoteData(response: BookmarksDTO) {
            response.resolvedBookmarks()?.map { it.toEntityModel() }?.let { bookmarksList ->
                bookmarksDao.deleteAll()
                bookmarksDao.insertAll(bookmarksList)
            }
        }

        override fun fetchFromLocal() = bookmarksDao.getAll().map { bookmarks ->
            bookmarks.map { it.toDomainModel() }
        }

        override suspend fun fetchFromRemote() = apiService.getBookmarks(
            xSessionId = xSession,
            url = "${serverUrl.removeTrailingSlash()}/api/bookmarks"
        )

        override fun shouldFetch(data: List<Bookmark>?) = true

    }.asFlow().flowOn(Dispatchers.IO)

    override fun getPagingBookmarks(
        xSession: String,
        serverUrl: String,
        searchText: String,
        tags: List<Tag>,
        saveToLocal: Boolean
    ): Flow<PagingData<Bookmark>> {
        return Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 2),
            pagingSourceFactory = {
                BookmarkPagingSource(
                    remoteDataSource = apiService,
                    bookmarksDao = bookmarksDao,
                    serverUrl = serverUrl,
                    xSessionId = xSession,
                    searchText = searchText,
                    tags = tags,
                    saveToLocal = saveToLocal
                )
            }
        ).flow
    }

    /**
     * Retrieves a paginated list of bookmarks from the local database using Room and Paging.
     *
     * Configurations:
     * - `pageSize = 30`: Suggests loading 30 items per page.
     * - `prefetchDistance = 2`: Prefetches 2 pages ahead of the currently loaded page.
     * - `enablePlaceholders = false`: Disables placeholders for unloaded items.
     *
     * Behavior:
     * - Although `pageSize` is set to 30, Room may initially load more items (90 in this case) as an optimization
     *   to reduce database queries and improve user experience during initial loads.
     * - Subsequent loads will fetch additional items in increments of 30 as the user scrolls.
     *
     * @param tags List of tags to filter bookmarks.
     * @param searchText Text to search bookmarks by title.
     * @return A Flow of paginated data to observe and update the UI as more data is loaded.
     */

    override fun getLocalPagingBookmarks(
        tags: List<Tag>,
        searchText: String
    ): Flow<PagingData<Bookmark>> {
        val processedSearchText = searchText.trim()
        val tagIds = tags.map { it.id }
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 2,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                when {
                    processedSearchText.isNotEmpty() && tagIds.isNotEmpty() -> {
                        bookmarksDao.getPagingBookmarks(searchText = processedSearchText, tagIds = tagIds)
                    }
                    processedSearchText.isNotEmpty() && tagIds.isEmpty() -> {
                        bookmarksDao.getPagingBookmarksWithoutTags(searchText = processedSearchText)
                    }
                    processedSearchText.isEmpty() && tagIds.isNotEmpty() -> {
                        bookmarksDao.getPagingBookmarksByTags(tagIds = tagIds)
                    }
                    else -> {
                        bookmarksDao.getAllPagingBookmarks()
                    }
                }
            }
        ).flow.map { pagingData ->
            pagingData.map {
                it.toDomainModel()
            }
        }
    }

    /**
     * Synchronizes all bookmarks from the remote server to the local database.
     *
     * This method performs a full synchronization of all bookmarks, regardless of the current
     * pagination state or user scroll position. It fetches all pages of bookmarks from the server
     * and updates the local database accordingly.
     *
     * @param xSession The session token for authentication with the remote API.
     * @param serverUrl The base URL of the server API.
     * @return Flow<SyncStatus> A flow emitting the current status of the synchronization process.
     *
     * The flow emits the following states:
     * - SyncStatus.Started: When the sync process begins.
     * - SyncStatus.InProgress(currentPage: Int): As each page is being fetched and processed.
     * - SyncStatus.Completed(totalBookmarks: Int): When all bookmarks have been successfully synced.
     * - SyncStatus.Error(error: Result.ErrorType): If an error occurs during the sync process.
     *
     * Note: This method performs a complete sync independently of RemoteMediator.
     * Use it for full synchronization when RemoteMediator's on-demand loading is insufficient.
     */
    override suspend fun syncAllBookmarks(
        xSession: String,
        serverUrl: String,
    ): Flow<SyncStatus> = flow {
        var currentPage = 1
        var hasNextPage = true
        val allBookmarks = mutableListOf<BookmarkEntity>()
        try {
            Log.d(TAG, "Sync started")
            emit(SyncStatus.Started)

            while (hasNextPage) {
                Log.d(TAG, "Fetching bookmarks for page $currentPage")
                emit(SyncStatus.InProgress(currentPage))
                val bookmarksDto = apiService.getPagingBookmarks(
                    xSessionId = xSession,
                    url = "${serverUrl.removeTrailingSlash()}/api/bookmarks?page=$currentPage"
                )
                Log.d(TAG, "Received response for page $currentPage with status: ${bookmarksDto.code()}")
                if (bookmarksDto.errorBody()?.string() == SESSION_HAS_BEEN_EXPIRED) {
                    Log.e(TAG, "Session has expired")
                    emit(SyncStatus.Error(Result.ErrorType.SessionExpired(message = SESSION_HAS_BEEN_EXPIRED)))
                    return@flow
                }
                val bookmarks = bookmarksDto.body()?.resolvedBookmarks()?.map { it.toEntityModel() } ?: emptyList()
                Log.d(TAG, "Fetched ${bookmarks.size} bookmarks for page $currentPage")
                allBookmarks.addAll(bookmarks)
                hasNextPage = hasNextPage(bookmarksDto)
                Log.d(TAG, "Has next page: $hasNextPage")
                if (hasNextPage) {
                    currentPage++
                }
            }
            Log.d(TAG, "Inserting ${allBookmarks.size} bookmarks into database")
            bookmarksDao.insertAllWithTags(allBookmarks)
            Log.d(TAG, "Sync completed with ${allBookmarks.size} bookmarks")
            emit(SyncStatus.Completed(allBookmarks.size))
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync: ${e.message}")
            emit(SyncStatus.Error(Result.ErrorType.Unknown(throwable = e)))
        }
    }

    private fun hasNextPage(bookmarksDto: Response<BookmarksDTO>): Boolean {
        val body = bookmarksDto.body() ?: return false
        val currentPage = body.resolvedPage() ?: return false
        val maxPage = body.resolvedMaxPage() ?: return false
        val bookmarks = body.resolvedBookmarks()

        return currentPage < maxPage && bookmarks?.isNotEmpty() == true
    }

    override suspend fun addBookmark(
        xSession: String,
        serverUrl: String,
        bookmark: Bookmark
    ): Bookmark {
        val response = apiService.addBookmark(
            url = "${serverUrl.removeTrailingSlash()}/api/bookmarks",
            xSessionId = xSession,
            body = bookmark.toAddBookmarkDTO().toJson()
        )
        if (response.isSuccessful) {
            response.body()?.let {
                return it.toDomainModel()
            }
            throw IllegalStateException("Response body is null")
        } else {
            throw IllegalStateException("Error adding bookmark: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Deletes a bookmark from the remote server.
     * The method uses a NetworkNoCacheResource to handle the network operation and error handling.
     *
     * @param xSession The session token for authentication with the remote API.
     * @param serverUrl The base URL of the server API.
     * @param bookmarkId The ID of the bookmark to be added.
     * @return A Flow emitting a Result<Bookmark> representing the outcome of the add operation.
     *         It can emit Loading, Success with the added bookmark, or Error states.
     */
    override suspend fun deleteBookmark(
        xSession: String,
        serverUrl: String,
        bookmarkId: Int
    ) {
        val response = apiService.deleteBookmarks(
            url = "${serverUrl.removeTrailingSlash()}/api/bookmarks",
            xSessionId = xSession,
            bookmarkIds = listOf(bookmarkId)
        )
        if (!response.isSuccessful) {
            throw IllegalStateException("Error deleting bookmark: ${response.errorBody()?.string()}")
        }
    }

    /**
     * Edits an existing bookmark both on the remote server and in the local database.
     *
     * This method performs the following steps:
     * 1. Sends an edit request to the remote server.
     * 2. If the server update is successful, updates the local database.
     * 3. Emits the updated bookmark if both operations are successful.
     *
     * The method uses a NetworkNoCacheResource to handle the network operation and error handling.
     *
     * @param xSession The session token for authentication with the remote API.
     * @param serverUrl The base URL of the server API.
     * @param bookmark The Bookmark object containing the updated information.
     * @return A Flow emitting a Result<Bookmark> representing the outcome of the edit operation.
     *         It can emit Loading, Success with the updated bookmark, or Error states.
     */
    override suspend fun editBookmark(
        xSession: String,
        serverUrl: String,
        bookmark: Bookmark
    ): Bookmark {
        val response = apiService.editBookmark(
            url = "${serverUrl.removeTrailingSlash()}/api/bookmarks",
            xSessionId = xSession,
            body = bookmark.toEditBookmarkDTO().toEditBookmarkJson()
        )
        if (response.isSuccessful) {
            response.body()?.let { bookmarkDTO ->
                // TODO force fields to avoid invalid backend response
                val updatedEntity = bookmarkDTO.toEntityModel().copy(
                    hasEbook = bookmark.hasEbook,
                    createEbook = bookmark.createEbook
                )
                bookmarksDao.updateBookmark(updatedEntity)
                return updatedEntity.toDomainModel()
            }
            throw IllegalStateException("Response body is null")
        } else {
            throw IllegalStateException("${response.errorBody()?.string()}")
        }
    }


    override suspend fun updateBookmarkCacheV1(
        token: String,
        serverUrl: String,
        updateCachePayload: UpdateCachePayload,
        bookmark: Bookmark?,
    ): List<Bookmark>  {
        val response = apiService.updateBookmarksCacheV1(
            url = "${serverUrl.removeTrailingSlash()}/api/v1/bookmarks/cache",
            authorization = "Bearer $token",
            body = updateCachePayload.toDTO().toJson()
        )
        if (response.isSuccessful) {
            response.body()?.let {
                it.message?.forEach { dto->
                    // TODO change to toEntityModel when backend is fixed
                    val updatedEntity = dto.toEntityModel().copy(
                        createEbook = if (updateCachePayload.createEbook) true else bookmark?.createEbook?: false,
                        createArchive = if (updateCachePayload.createArchive) true else bookmark?.createArchive?: false,
                        hasEbook = if (updateCachePayload.createEbook) true else bookmark?.hasEbook?: false,
                        hasArchive = if (updateCachePayload.createArchive) true else bookmark?.hasArchive?: false,
                        modified = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    )
                    bookmarksDao.updateBookmark(updatedEntity)
                }
               return  it.message?.map { it.toDomainModel() }?: emptyList()
            }
            throw IllegalStateException("Response body is null")
        } else {
            throw IllegalStateException("${response.errorBody()?.string()}")
        }
    }

    override suspend fun deleteAllLocalBookmarks()  { bookmarksDao.deleteAll() }

    override fun getBookmarkReadableContent(
        token: String,
        serverUrl: String,
        bookmarkId: Int
    ) = object :
        NetworkNoCacheResource<ReadableContentResponseDTO, ReadableContent>(errorHandler = errorHandler) {
        override suspend fun fetchFromRemote(): Response<ReadableContentResponseDTO> = apiService.getBookmarkReadableContent(
            url = "${serverUrl.removeTrailingSlash()}/api/v1/bookmarks/${bookmarkId}/readable",
            authorization = "Bearer $token",
        )

        override fun fetchResult(data: ReadableContentResponseDTO): Flow<ReadableContent> {
            return flow {
                    emit(data.toDomainModel())
            }
        }
    }.asFlow().flowOn(Dispatchers.IO)

    /**
     * Syncs the bookmarks between the remote server and the local database.
     *
     * This method performs the following steps:
     * 1. Sends a sync request to the remote server.
     * 2. If the server update is successful, updates the local database.
     * 3. Emits the sync status if both operations are successful.
     *
     * The method uses a NetworkNoCacheResource to handle the network operation and error handling.
     *
     * @param token The session token for authentication with the remote API.
     * @param serverUrl The base URL of the server API.
     * @param syncBookmarksRequestPayload The payload containing the bookmarks to be synced.
     * @return A Flow emitting a Result<SyncBookmarksResponse> representing the outcome of the sync operation.
     *         It can emit Loading, Success with the sync result, or Error states.
     */
    override fun syncBookmarks(
        token: String,
        serverUrl: String,
        syncBookmarksRequestPayload: SyncBookmarksRequestPayload
    ): Flow<Result<SyncBookmarksResponse>> {
        return object : NetworkNoCacheResource<SyncBookmarksResponseDTO, SyncBookmarksResponse>(errorHandler = errorHandler) {
            override suspend fun fetchFromRemote(): Response<SyncBookmarksResponseDTO> {
                return apiService.syncBookmarks(
                    url = "${serverUrl.removeTrailingSlash()}/api/v1/bookmarks/sync",
                    authorization = "Bearer $token",
                    body = syncBookmarksRequestPayload.toJson()
                )
            }

            override fun fetchResult(data: SyncBookmarksResponseDTO): Flow<SyncBookmarksResponse> {
                return flow {
                    emit(data.toDomainModel())
                }
            }
        }.asFlow().flowOn(Dispatchers.IO)
    }

    override fun getBookmarkById(
        token: String,
        serverUrl: String,
        bookmarkId: Int
    ) = object :
        NetworkNoCacheResource<BookmarkDTO, Bookmark>(errorHandler = errorHandler) {

        override suspend fun fetchFromRemote(): Response<BookmarkDTO> = apiService.getBookmark(
            url = "${serverUrl.removeTrailingSlash()}/api/v1/bookmarks/$bookmarkId",
            authorization = "Bearer $token",
        )

        override fun fetchResult(data: BookmarkDTO): Flow<Bookmark> {
            return flow {
                emit(data.toDomainModel())
            }
        }
    }.asFlow().flowOn(Dispatchers.IO)

}

sealed class SyncStatus {
    data object Started : SyncStatus()
    data class InProgress(val currentPage: Int) : SyncStatus()
    data class Completed(val totalSynced: Int) : SyncStatus()
    data class Error(val error: Result.ErrorType) : SyncStatus()
}

