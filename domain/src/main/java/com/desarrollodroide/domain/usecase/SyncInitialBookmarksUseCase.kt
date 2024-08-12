package com.desarrollodroide.domain.usecase

import android.annotation.SuppressLint
import android.util.Log
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.data.repository.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch

class SyncInitialBookmarksUseCase(
    private val bookmarksRepository: BookmarksRepository,
) {
    private val TAG = "SyncInitialBookmarksUseCase"

    @SuppressLint("LongLogTag")
    suspend operator fun invoke(
        serverUrl: String,
        xSession: String,
    ): Flow<Result<SyncStatus>> {
        Log.d(TAG, "Invoking sync use case")
        return bookmarksRepository.syncAllBookmarks(
            xSession = xSession,
            serverUrl = serverUrl
        )
            .map { status ->
                Log.d(TAG, "Mapping sync status: $status")
                Result.success(status)
            }
            .catch { e ->
                Log.e(TAG, "Error caught in use case", e)
                emit(Result.failure(e))
            }
            .flowOn(Dispatchers.IO)
    }
}


