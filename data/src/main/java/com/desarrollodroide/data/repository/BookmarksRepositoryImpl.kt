package com.desarrollodroide.data.repository

import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.data.extensions.toBodyJson
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.mapper.*
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.network.model.BookmarkDTO
import com.desarrollodroide.network.model.BookmarksDTO
import com.desarrollodroide.network.retrofit.NetworkBoundResource
import com.desarrollodroide.network.retrofit.NetworkNoCacheResource
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

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
            url = "$serverUrl/api/bookmarks"
        )

        override fun shouldFetch(data: List<Bookmark>?) = true

    }.asFlow().flowOn(Dispatchers.IO)


    override fun addBookmark(
        xSession: String,
        serverUrl: String,
        bookmark: Bookmark
    ) = object :
        NetworkNoCacheResource<BookmarkDTO, Bookmark>(errorHandler = errorHandler) {
        override suspend fun fetchFromRemote() = apiService.addBookmark(
            url = "$serverUrl/api/bookmarks",
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
                    url = "$serverUrl/api/bookmarks",
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
            url = "$serverUrl/api/bookmarks",
            xSessionId = xSession,
            body = bookmark.toBodyJson()
        )

        override fun fetchResult(data: BookmarkDTO): Flow<Bookmark> {
            return flow {
                emit(data.toDomainModel())
            }
        }
    }.asFlow().flowOn(Dispatchers.IO)
}

