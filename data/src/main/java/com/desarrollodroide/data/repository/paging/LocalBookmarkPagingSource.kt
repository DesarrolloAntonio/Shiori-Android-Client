package com.desarrollodroide.data.repository.paging

import android.annotation.SuppressLint
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.mapper.toDomainModel
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag

//@SuppressLint("LongLogTag")
//class LocalBookmarkPagingSource(
//    private val bookmarksDao: BookmarksDao,
//    private val searchText: String,
//    private val tags: List<Tag>
//) : PagingSource<Int, Bookmark>() {
//
//    companion object {
//        private const val TAG = "LocalBookmarkPagingSource"
//        private const val STARTING_PAGE_INDEX = 0
//        private const val PAGE_SIZE = 30
//    }
//
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Bookmark> {
//        return try {
//            val page = params.key ?: STARTING_PAGE_INDEX
//            val offset = page * PAGE_SIZE
//
//            Log.d(TAG, "Loading page: $page, pageSize: $PAGE_SIZE, offset: $offset")
//            Log.d(TAG, "Search text: '$searchText', Tags: ${tags.map { it.name }.joinToString()}")
//
//            val bookmarks = bookmarksDao.getPagingBookmarks(
//                searchText = searchText,
//                tags = tags.map { it.name },
//                tagsListSize = tags.size,
//                limit = PAGE_SIZE,
//                offset = offset
//            )
//
//            Log.d(TAG, "Loaded ${bookmarks.size} bookmarks")
//
//            val totalCount = bookmarksDao.getPagingBookmarksCount(
//                searchText = searchText,
//                tags = tags.map { it.name },
//                tagsListSize = tags.size
//            )
//
//            Log.d(TAG, "Total count of bookmarks matching criteria: $totalCount")
//
//            val nextKey = if (offset + bookmarks.size < totalCount) page + 1 else null
//            val prevKey = if (page > 0) page - 1 else null
//
//            Log.d(TAG, "Next key: $nextKey, Previous key: $prevKey")
//
//            LoadResult.Page(
//                data = bookmarks.map { it.toDomainModel() }.also {
//                    Log.d(TAG, "Mapped ${it.size} bookmarks to domain model")
//                },
//                prevKey = prevKey,
//                nextKey = nextKey
//            )
//        } catch (e: Exception) {
//            Log.e(TAG, "Error loading bookmarks", e)
//            LoadResult.Error(e)
//        }
//    }
//
//    override fun getRefreshKey(state: PagingState<Int, Bookmark>): Int? {
//        return state.anchorPosition?.let { anchorPosition ->
//            val anchorPage = state.closestPageToPosition(anchorPosition)
//            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
//        }.also { Log.d(TAG, "Refresh key: $it") }
//    }
//}
