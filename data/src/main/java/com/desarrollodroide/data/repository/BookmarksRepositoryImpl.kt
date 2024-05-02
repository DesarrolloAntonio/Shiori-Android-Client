package com.desarrollodroide.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.data.extensions.toBodyJson
import com.desarrollodroide.data.extensions.toJson
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.mapper.*
import com.desarrollodroide.data.repository.paging.MoviePagingSource
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.UpdateCachePayload
import com.desarrollodroide.network.model.BookmarkDTO
import com.desarrollodroide.network.model.BookmarkResponseDTO
import com.desarrollodroide.network.model.BookmarksDTO
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
        searchText: String
    ): Flow<PagingData<Bookmark>> {
        return Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 2),
            pagingSourceFactory = {
                MoviePagingSource(
                    remoteDataSource = apiService,
                    bookmarksDao = bookmarksDao,
                    serverUrl = serverUrl,
                    xSessionId = xSession,
                    searchText = searchText,
                )
            }
        ).flow
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
}

