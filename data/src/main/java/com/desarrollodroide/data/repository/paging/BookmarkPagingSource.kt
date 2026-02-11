package com.desarrollodroide.data.repository.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.data.helpers.SESSION_HAS_BEEN_EXPIRED
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.mapper.toDomainModel
import com.desarrollodroide.data.mapper.toEntityModel
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class BookmarkPagingSource(
    private val remoteDataSource: RetrofitNetwork,
    private val bookmarksDao: BookmarksDao,
    private val serverUrl: String,
    private val xSessionId: String,
    private val searchText: String,
    private val tags: List<Tag>,
    private val saveToLocal: Boolean,
) : PagingSource<Int, Bookmark>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Bookmark> {
        return try {
            val page = params.key ?: 1
            val pageSize = params.loadSize // Not needed
            val searchKeywordsParams = if (searchText.isNotEmpty())"&keyword=$searchText" else ""
            val searchTagsParams = if (tags.isNotEmpty())"&tags=${tags.joinToString(",") { it.name }}" else ""
            val bookmarksDto = remoteDataSource.getPagingBookmarks(
                xSessionId = xSessionId,
                url = "${serverUrl.removeTrailingSlash()}/api/bookmarks?page=$page$searchKeywordsParams$searchTagsParams",
            )
            if (bookmarksDto.errorBody()?.string() == SESSION_HAS_BEEN_EXPIRED) {
                return LoadResult.Error(Exception(SESSION_HAS_BEEN_EXPIRED))
            }
            if (saveToLocal){
                bookmarksDto.body()?.resolvedBookmarks()?.map { it.toEntityModel() }?.let { bookmarksList ->
                    if (page == 1) {
                        bookmarksDao.deleteAll()
                    }
                    bookmarksDao.insertAll(bookmarksList)
                }
            }
            val bookmarks = bookmarksDto.body()?.resolvedBookmarks()?.map { it.toDomainModel() }?: emptyList()
            LoadResult.Page(
                data = bookmarks,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if ((bookmarksDto.body()?.resolvedPage() ?: 0) >= (bookmarksDto.body()?.resolvedMaxPage() ?: 0)) null else page + 1
            )
        } catch (exception: IOException) {
            Log.e("BookmarkPagingSource", "IOException", exception)
            return loadFromLocalWhenError()
        } catch (exception: HttpException) {
            Log.e("BookmarkPagingSource", "HttpException", exception)
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