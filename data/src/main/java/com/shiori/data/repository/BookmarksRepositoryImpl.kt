package com.shiori.data.repository

import com.shiori.common.result.ErrorHandler
import com.shiori.common.result.Result
import com.shiori.data.extensions.toBodyJson
import com.shiori.data.helpers.toJson
import com.shiori.data.local.room.dao.BookmarksDao
import com.shiori.data.mapper.*
import com.shiori.model.Bookmark
import com.shiori.network.model.BookmarkDTO
import com.shiori.network.model.BookmarksDTO
import com.shiori.network.retrofit.NetworkBoundResource
import com.shiori.network.retrofit.NetworkNoCacheResource
import com.shiori.network.retrofit.RetrofitNetwork
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

