package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.data.repository.SyncWorks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SendLogoutUseCase(
    private val authRepository: AuthRepository,
    private val syncManager: SyncWorks,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val bookmarksRepository: BookmarksRepository
) {
    operator fun invoke(
        serverUrl: String,
        xSession: String,
    ): Flow<Result<String?>> = flow {
        authRepository.sendLogout(
            serverUrl = serverUrl,
            xSession = xSession
        ).collect { result ->
            when (result) {
                is Result.Success -> {
                    performCleanup()
                    emit(Result.Success(result.data))
                }
                is Result.Error -> {
                    performCleanup()
                    emit(Result.Error(result.error, result.data))
                }
                is Result.Loading -> {
                    emit(result)
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun performCleanup() {
        syncManager.cancelAllSyncWorkers()
        settingsPreferenceDataSource.resetData()
        bookmarksRepository.deleteAllLocalBookmarks()
    }
}
