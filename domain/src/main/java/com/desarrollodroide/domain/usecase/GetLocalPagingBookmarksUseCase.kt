package com.desarrollodroide.domain.usecase

import androidx.paging.PagingData
import androidx.paging.filter
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetLocalPagingBookmarksUseCase(
    private val bookmarksRepository: BookmarksRepository,
) {
    operator fun invoke(
        serverUrl: String,
        xSession: String,
        searchText: String = "",
        tags: List<Tag>,
        showOnlyHiddenTag: Boolean = false,
        tagToHide: Tag? = null
    ): Flow<PagingData<Bookmark>> {
        return bookmarksRepository.getLocalPagingBookmarks(tags, searchText)
            .map { pagingData ->
                pagingData.filter { bookmark ->
                    when {
                        showOnlyHiddenTag -> tagToHide?.let { bookmark.tags.any { tag -> tag.id == it.id } } ?: false
                        tagToHide != null -> bookmark.tags.none { it.id == tagToHide.id }
                        else -> true
                    }
                }
            }

        // Then, if a connection is available, attempt to synchronize with the remote server.
        // TODO create sync logic
    }
}

