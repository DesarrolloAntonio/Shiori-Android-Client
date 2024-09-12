package com.desarrollodroide.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.data.extensions.toBodyJson
import com.desarrollodroide.data.extensions.toJson
import com.desarrollodroide.data.helpers.SESSION_HAS_BEEN_EXPIRED
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.mapper.*
import com.desarrollodroide.data.repository.paging.BookmarkPagingSource
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.ReadableContent
import com.desarrollodroide.model.Tag
import com.desarrollodroide.model.UpdateCachePayload
import com.desarrollodroide.network.model.BookmarkDTO
import com.desarrollodroide.network.model.BookmarkResponseDTO
import com.desarrollodroide.network.model.BookmarksDTO
import com.desarrollodroide.network.model.ReadableContentResponseDTO
import com.desarrollodroide.network.retrofit.NetworkBoundResource
import com.desarrollodroide.network.retrofit.NetworkNoCacheResource
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import retrofit2.Response

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
            response.bookmarks?.map { it.toEntityModel() }?.let { bookmarksList ->
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

    override fun getLocalPagingBookmarks(tags: List<Tag>, searchText: String): Flow<PagingData<Bookmark>> {

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
                Log.v("Bookmark", it.title)
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
                val bookmarks = bookmarksDto.body()?.bookmarks?.map { it.toEntityModel() } ?: emptyList()
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
        val currentPage = body.page ?: return false
        val maxPage = body.maxPage ?: return false
        val bookmarks = body.bookmarks

        return currentPage < maxPage && bookmarks?.isNotEmpty() == true
    }

    override fun addBookmark(
        xSession: String,
        serverUrl: String,
        bookmark: Bookmark
    ) = object :
        NetworkNoCacheResource<BookmarkDTO, Bookmark>(errorHandler = errorHandler) {
        override suspend fun fetchFromRemote() = apiService.addBookmark(
            url = "${serverUrl.removeTrailingSlash()}/api/bookmarks",
            xSessionId = xSession,
            body = bookmark.toBodyJson()
        )

        override fun fetchResult(data: BookmarkDTO): Flow<Bookmark> {
            return flow {
                emit(data.toDomainModel())
            }
        }
    }.asFlow().flowOn(Dispatchers.IO)


    override fun deleteBookmark(
        xSession: String,
        serverUrl: String,
        bookmarkId: Int
    ) = object : NetworkNoCacheResource<Unit, Unit>(errorHandler = errorHandler) {
            override suspend fun fetchFromRemote()  =
                apiService.deleteBookmarks(
                    url = "${serverUrl.removeTrailingSlash()}/api/bookmarks",
                    xSessionId = xSession,
                    bookmarkIds = listOf(bookmarkId)
                )

            override fun fetchResult(data: Unit): Flow<Unit> =
                flow { emit(Unit) }
        }.asFlow().flowOn(Dispatchers.IO)

    override fun editBookmark(
        xSession: String,
        serverUrl: String,
        bookmark: Bookmark
    ) = object :
        NetworkNoCacheResource<BookmarkDTO, Bookmark>(errorHandler = errorHandler) {
        override suspend fun fetchFromRemote() = apiService.editBookmark(
            url = "${serverUrl.removeTrailingSlash()}/api/bookmarks",
            xSessionId = xSession,
            body = bookmark.toBodyJson()
        )

        override fun fetchResult(data: BookmarkDTO): Flow<Bookmark> {
            return flow {
                emit(data.toDomainModel())
            }
        }
    }.asFlow().flowOn(Dispatchers.IO)

    override fun updateBookmarkCache(
        xSession: String,
        serverUrl: String,
        updateCachePayload: UpdateCachePayload
    ) = object :
        NetworkNoCacheResource<List<BookmarkDTO>, Bookmark>(errorHandler = errorHandler) {
        override suspend fun fetchFromRemote(): Response<List<BookmarkDTO>> = apiService.updateBookmarksCache(
            url = "${serverUrl.removeTrailingSlash()}/api/cache",
            xSessionId = xSession,
            body = updateCachePayload.toDTO().toJson()
        )

        override fun fetchResult(data: List<BookmarkDTO>): Flow<Bookmark> {
            return flow {
                data.firstOrNull()?.let {
                    emit(it.toDomainModel())
                }
            }
        }
    }.asFlow().flowOn(Dispatchers.IO)


    override fun updateBookmarkCacheV1(
        token: String,
        serverUrl: String,
        updateCachePayload: UpdateCachePayload
    ) = object :
        NetworkNoCacheResource<BookmarkResponseDTO, Bookmark>(errorHandler = errorHandler) {
        override suspend fun fetchFromRemote(): Response<BookmarkResponseDTO> = apiService.updateBookmarksCacheV1(
            url = "${serverUrl.removeTrailingSlash()}/api/v1/bookmarks/cache",
            authorization = "Bearer $token",
            body = updateCachePayload.toV1DTO().toJson()
        )

        override fun fetchResult(data: BookmarkResponseDTO): Flow<Bookmark> {
            return flow {
                data.message?.firstOrNull()?.let {
                    emit(it.toDomainModel())
                }
            }
        }
    }.asFlow().flowOn(Dispatchers.IO)

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
}

sealed class SyncStatus {
    data object Started : SyncStatus()
    data class InProgress(val currentPage: Int) : SyncStatus()
    data class Completed(val totalSynced: Int) : SyncStatus()
    data class Error(val error: Result.ErrorType) : SyncStatus()
}

