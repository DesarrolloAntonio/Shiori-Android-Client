package com.desarrollodroide.domain.usecase

import androidx.paging.PagingData
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import kotlinx.coroutines.flow.Flow

class GetPagingBookmarksUseCase(
    private val bookmarksRepository: BookmarksRepository,
) {
    operator fun invoke(
        serverUrl: String,
        xSession: String,
        searchText: String = "",
        tags: List<Tag>,
        saveToLocal: Boolean
    ): Flow<PagingData<Bookmark>> {
        return bookmarksRepository.getPagingBookmarks(
            xSession = xSession,
            serverUrl = serverUrl,
            searchText = searchText,
            tags = tags,
            saveToLocal = saveToLocal
        )
    }
}

