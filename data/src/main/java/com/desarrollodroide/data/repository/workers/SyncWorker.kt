package com.desarrollodroide.data.repository.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.data.mapper.toDomainModel
import com.desarrollodroide.data.mapper.toEntityModel
import com.desarrollodroide.data.repository.AuthRepository
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.SyncOperationType
import kotlinx.coroutines.flow.first
import org.koin.core.component.inject
import org.koin.core.component.KoinComponent
import com.desarrollodroide.data.helpers.SESSION_HAS_BEEN_EXPIRED
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.firstOrNull

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val bookmarksRepository: BookmarksRepository by inject()
    private val bookmarksDao: BookmarksDao by inject()
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource by inject()
    private val authRepository: AuthRepository by inject()

    override suspend fun doWork(): Result {
        val operationType = inputData.getString("operationType")?.let { SyncOperationType.valueOf(it) }
        val bookmarkId = inputData.getInt("bookmarkId", -1)

        if (operationType == null || bookmarkId == -1) {
            return Result.failure()
        }

        return try {
            val xSession = settingsPreferenceDataSource.getSession()
            val serverUrl = settingsPreferenceDataSource.getUrl()

            try {
                performSyncOperation(xSession, serverUrl, operationType, bookmarkId)
                Log.v("SyncWorker", "Sync completed successfully")
                Result.success()
            } catch (e: Exception) {
                if (isSessionExpiredException(e)) {
                    val sessionRefreshed = refreshSession()
                    if (sessionRefreshed) {
                        try {
                            val newSession = settingsPreferenceDataSource.getSession()
                            performSyncOperation(newSession, serverUrl, operationType, bookmarkId)
                            Log.v("SyncWorker", "Sync completed successfully after session refresh")
                            Result.success()
                        } catch (retryException: Exception) {
                            Log.e("SyncWorker", "Error after session refresh: ${retryException.message}")
                            Result.retry()
                        }
                    } else {
                        Log.e("SyncWorker", "Failed to refresh session")
                        Result.retry()
                    }
                } else {
                    Log.e("SyncWorker", "Error during sync: ${e.message}", e)
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Unexpected error: ${e.message}")
            Result.retry()
        }
    }


    private suspend fun refreshSession(): Boolean {
        val serverUrl = settingsPreferenceDataSource.getUrl()
        val rememberedUser = settingsPreferenceDataSource.getUser().first()

        if (rememberedUser.account.userName.isEmpty() || rememberedUser.account.password.isEmpty()) {
            return false
        }

        return authRepository.sendLoginV1(
            username = rememberedUser.account.userName,
            password = rememberedUser.account.password,
            serverUrl = serverUrl
        )
            .filterNot { it is com.desarrollodroide.common.result.Result.Loading }
            .firstOrNull()?.let { result ->
                when (result) {
                    is com.desarrollodroide.common.result.Result.Success -> true
                    else -> false
                }
            } ?: false
    }

    private suspend fun performSyncOperation(
        xSession: String,
        serverUrl: String,
        operationType: SyncOperationType,
        bookmarkId: Int
    ) {
        when (operationType) {
            SyncOperationType.CREATE -> {
                val updatedBookmark = syncCreateBookmark(xSession, serverUrl, bookmarkId)
                bookmarksDao.deleteBookmarkById(bookmarkId)
                bookmarksDao.insertBookmark(updatedBookmark.toEntityModel())
            }
            SyncOperationType.UPDATE -> syncUpdateBookmark(xSession, serverUrl, bookmarkId)
            SyncOperationType.DELETE -> syncDeleteBookmark(xSession, serverUrl, bookmarkId)
        }
    }

    private fun isSessionExpiredException(e: Exception): Boolean {
        return e.message?.contains(SESSION_HAS_BEEN_EXPIRED) == true
    }

    private suspend fun syncCreateBookmark(xSession: String, serverUrl: String, bookmarkId: Int): Bookmark {
        val bookmark = bookmarksDao.getBookmarkById(bookmarkId)?.toDomainModel()
            ?: throw IllegalStateException("Bookmark not found for ID: $bookmarkId")
        return bookmarksRepository.addBookmark(xSession, serverUrl, bookmark)
    }

    private suspend fun syncUpdateBookmark(xSession: String, serverUrl: String, bookmarkId: Int) {
        val bookmark = bookmarksDao.getBookmarkById(bookmarkId)?.toDomainModel()
            ?: throw IllegalStateException("Bookmark not found for ID: $bookmarkId")
        bookmarksRepository.editBookmark(xSession, serverUrl, bookmark)
    }

    private suspend fun syncDeleteBookmark(xSession: String, serverUrl: String, bookmarkId: Int) {
        bookmarksRepository.deleteBookmark(xSession, serverUrl, bookmarkId)
    }

    class Factory : WorkerFactory(), KoinComponent {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker? {
            return when (workerClassName) {
                SyncWorker::class.java.name -> SyncWorker(appContext, workerParameters)
                else -> null
            }
        }
    }
}
