package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.model.SyncBookmarksRequestPayload
import com.desarrollodroide.model.SyncBookmarksResponse
import kotlinx.coroutines.flow.Flow
import com.desarrollodroide.common.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class SyncBookmarksUseCase(
    private val bookmarksRepository: BookmarksRepository
) {
    operator fun invoke(
        xSessionId: String,
        serverUrl: String,
        syncBookmarksRequestPayload: SyncBookmarksRequestPayload
    ): Flow<Result<SyncBookmarksResponse>> {
        return bookmarksRepository.syncBookmarks(
            xSession = xSessionId,
            serverUrl = serverUrl,
            syncBookmarksRequestPayload = syncBookmarksRequestPayload
        ).flowOn(Dispatchers.IO)
    }
}