package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.model.Bookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import com.desarrollodroide.common.result.Result

class GetBookmarksUseCase(
    private val bookmarksRepository: BookmarksRepository,
) {
    operator fun invoke(
        serverUrl: String,
        xSession: String,
    ): Flow<Result<List<Bookmark>?>> {
        return bookmarksRepository.getBookmarks(
            xSession = xSession,
            serverUrl = serverUrl
        ).flowOn(Dispatchers.IO)
    }
}