package com.shiori.domain.usecase

import com.shiori.data.repository.BookmarksRepository
import com.shiori.model.Bookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import com.shiori.common.result.Result

class EditBookmarkUseCase(
    private val bookmarksRepository: BookmarksRepository,
) {
    operator fun invoke(
        serverUrl: String,
        xSession: String,
        bookmark: Bookmark
    ): Flow<Result<Bookmark?>> {
        return bookmarksRepository.editBookmark(
            xSession = xSession,
            serverUrl = serverUrl,
            bookmark = bookmark
        ).flowOn(Dispatchers.IO)
    }
}