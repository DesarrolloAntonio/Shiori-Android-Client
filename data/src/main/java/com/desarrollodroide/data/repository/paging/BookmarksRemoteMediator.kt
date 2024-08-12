package com.desarrollodroide.data.repository.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.data.helpers.SESSION_HAS_BEEN_EXPIRED
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.mapper.toDomainModel
import com.desarrollodroide.data.mapper.toEntityModel
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalPagingApi::class)
class BookmarksRemoteMediator(
    private val apiService: RetrofitNetwork,
    private val bookmarksDao: BookmarksDao,
    private val serverUrl: String,
    private val xSessionId: String,
    private val searchText: String,
    private val tags: List<Tag>
) : RemoteMediator<Int, Bookmark>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Bookmark>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        1
                    } else {
                        (lastItem.id / state.config.pageSize) + 1
                    }
                }
            }

            val response = apiService.getPagingBookmarks(
                xSessionId = xSessionId,
                url = "${serverUrl.removeTrailingSlash()}/api/bookmarks?page=$page&keyword=$searchText&tags=${tags.joinToString(",") { it.name }}",
            )

            if (response.isSuccessful) {
                val bookmarksDto = response.body()
                val bookmarks = bookmarksDto?.bookmarks?.map { it.toEntityModel() } ?: emptyList()

                if (loadType == LoadType.REFRESH) {
                    bookmarksDao.deleteAll()
                }
                bookmarksDao.insertAll(bookmarks)

                val endOfPaginationReached = (bookmarksDto?.page ?: 0) >= (bookmarksDto?.maxPage ?: 0)

                MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            } else {
                if (response.errorBody()?.string() == SESSION_HAS_BEEN_EXPIRED) {
                    MediatorResult.Error(Exception(SESSION_HAS_BEEN_EXPIRED))
                } else {
                    MediatorResult.Error(Exception("Error loading data"))
                }
            }
        } catch (e: Exception) {
            // If there's a network error, we load data from local database
            val localBookmarks = loadFromLocalWhenError()
            MediatorResult.Success(endOfPaginationReached = true)
        }
    }

    private suspend fun loadFromLocalWhenError(): List<Bookmark> {
        return bookmarksDao.getAll()
            .first()
            .map { it.toDomainModel() }
            .reversed()
    }
}