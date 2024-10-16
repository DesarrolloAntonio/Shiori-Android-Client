package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.data.repository.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SendLogoutUseCase(
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager,
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
                    syncManager.cancelAllSyncWorkers()
                    settingsPreferenceDataSource.resetUser()
                    bookmarksRepository.deleteAllLocalBookmarks()
                    emit(Result.Success(result.data))
                }
                is Result.Error -> {
                    settingsPreferenceDataSource.resetUser()
                    bookmarksRepository.deleteAllLocalBookmarks()
                    emit(Result.Error<String?>(result.error, result.data))
                }
                is Result.Loading -> {
                    emit(result)
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
