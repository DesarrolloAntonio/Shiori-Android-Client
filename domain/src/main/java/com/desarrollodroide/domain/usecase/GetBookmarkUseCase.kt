package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.model.Bookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import com.desarrollodroide.common.result.Result

class GetBookmarkByIdUseCase(
    private val bookmarksRepository: BookmarksRepository,
) {
    operator fun invoke(
        serverUrl: String,
        token: String,
        bookmarkId: Int
    ): Flow<Result<Bookmark?>> {
        return bookmarksRepository.getBookmarkById(
            token = token,
            serverUrl = serverUrl,
            bookmarkId = bookmarkId
        ).flowOn(Dispatchers.IO)
    }
}