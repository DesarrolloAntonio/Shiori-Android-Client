package com.desarrollodroide.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.model.ReadableContent

class GetBookmarkReadableContentUseCase(
    private val bookmarksRepository: BookmarksRepository
) {
    operator fun invoke(
        serverUrl: String,
        token: String,
        bookmarkId: Int
    ): Flow<Result<ReadableContent>> {
        return bookmarksRepository.getBookmarkReadableContent(
            token = token,
            serverUrl = serverUrl,
            bookmarkId = bookmarkId
        ).flowOn(Dispatchers.IO)
    }
}