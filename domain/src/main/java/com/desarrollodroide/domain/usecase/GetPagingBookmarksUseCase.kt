package com.desarrollodroide.domain.usecase

import androidx.paging.PagingData
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetPagingBookmarksUseCase(
    private val bookmarksRepository: BookmarksRepository,
) {
    operator fun invoke(
        serverUrl: String,
        xSession: String,
        searchText: String = "",
        tags: List<Tag>,
    ): Flow<PagingData<Bookmark>> {
        val localDataFlow = bookmarksRepository.getLocalPagingBookmarks(tags, searchText)

        // Then, if a connection is available, attempt to synchronize with the remote server.
        // TODO create sync logic


       return localDataFlow
    }

}

