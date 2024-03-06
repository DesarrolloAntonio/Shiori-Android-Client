package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.model.Bookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.model.UpdateCachePayload

class UpdateBookmarkCacheUseCase(
    private val bookmarksRepository: BookmarksRepository,
) {
    operator fun invoke(
        serverUrl: String,
        xSession: String,
        token: String,
        updateCachePayload: UpdateCachePayload,
        isLegacyApi: Boolean,
        ): Flow<Result<Bookmark?>> {

        return if (isLegacyApi)
            bookmarksRepository.updateBookmarkCache(
                xSession = xSession,
                serverUrl = serverUrl,
                updateCachePayload = updateCachePayload
            ).flowOn(Dispatchers.IO)
        else
            bookmarksRepository.updateBookmarkCacheV1(
                token = token,
                serverUrl = serverUrl,
                updateCachePayload = updateCachePayload
            ).flowOn(Dispatchers.IO)
    }
}