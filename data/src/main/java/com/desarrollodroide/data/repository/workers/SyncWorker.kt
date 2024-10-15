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
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.data.repository.SyncOperationType
import com.desarrollodroide.model.Bookmark
import org.koin.core.component.inject
import org.koin.core.component.KoinComponent

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val bookmarksRepository: BookmarksRepository by inject()
    private val bookmarksDao: BookmarksDao by inject()
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource by inject()

    override suspend fun doWork(): Result {
        val operationType = inputData.getString("operationType")?.let { SyncOperationType.valueOf(it) }
        val bookmarkId = inputData.getInt("bookmarkId", -1)

        if (operationType == null || bookmarkId == -1) {
            return Result.failure()
        }

        return try {
            val xSession = settingsPreferenceDataSource.getSession()
            val serverUrl = settingsPreferenceDataSource.getUrl()

            when (operationType) {
                SyncOperationType.CREATE -> {
                    val updatedBookmark = syncCreateBookmark(xSession, serverUrl, bookmarkId)
                    // Update the local bookmark with the server response, including the new ID
                    bookmarksDao.deleteBookmarkById(bookmarkId)
                    bookmarksDao.insertBookmark(updatedBookmark.toEntityModel())
                }
                SyncOperationType.UPDATE -> {
                    syncUpdateBookmark(xSession, serverUrl, bookmarkId)
                }
                SyncOperationType.DELETE -> {
                    syncDeleteBookmark(xSession, serverUrl, bookmarkId)
                }
            }

            Log.v("SyncWorker", "Sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error during sync: ${e.message}", e)
            Result.retry()
        }
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
