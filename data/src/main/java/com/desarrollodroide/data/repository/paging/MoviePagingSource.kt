package com.desarrollodroide.data.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.mapper.toDomainModel
import com.desarrollodroide.data.mapper.toEntityModel
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class MoviePagingSource(
    private val remoteDataSource: RetrofitNetwork,
    private val bookmarksDao: BookmarksDao,
    private val serverUrl: String,
    private val xSessionId: String
) : PagingSource<Int, Bookmark>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Bookmark> {
        return try {
            val page = params.key ?: 1
            val pageSize = params.loadSize // Not needed
            val bookmarksDto = remoteDataSource.getPagingBookmarks(
                xSessionId = xSessionId,
                //url = "${serverUrl.removeTrailingSlash()}/api/bookmarks?page=$page&keyword=app&tags=test2+test1",
                url = "${serverUrl.removeTrailingSlash()}/api/bookmarks?page=$page",
            )
            bookmarksDto.bookmarks?.map { it.toEntityModel() }?.let { bookmarksList ->
                if (page == 1) {
                    bookmarksDao.deleteAll()
                }
                bookmarksDao.insertAll(bookmarksList)
            }
            val bookmarks = bookmarksDto.bookmarks?.map { it.toDomainModel() }?: emptyList()
            LoadResult.Page(
                data = bookmarks,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if ((bookmarksDto.page ?: 0) >= (bookmarksDto.maxPage ?: 0)) null else page + 1
            )
        } catch (exception: IOException) {
            return loadFromLocalWhenError()
        } catch (exception: HttpException) {
            return loadFromLocalWhenError()
        }
    }

    private suspend fun loadFromLocalWhenError(): LoadResult.Page<Int, Bookmark> {
        val bookmarks = bookmarksDao.getAll().map { bookmarks ->
            bookmarks.map { it.toDomainModel() }
        }.first().reversed()
        return LoadResult.Page(
            data = bookmarks,
            prevKey = null,
            nextKey = null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Bookmark>): Int? {
        return state.anchorPosition
    }

}