package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.local.room.dao.BookmarksDao
import com.desarrollodroide.model.Bookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import com.desarrollodroide.common.result.Result

class DeleteLocalBookmarkUseCase(
    private val bookmarksDao: BookmarksDao
) {
    operator fun invoke(bookmark: Bookmark): Flow<Result<Int>> = flow {
        emit(Result.Loading())
        try {
            val result = bookmarksDao.deleteBookmarkById(bookmark.id)
            if (result > 0) {
                emit(Result.Success(result))
            } else {
                emit(Result.Error(Result.ErrorType.DatabaseError(BookmarkNotFoundException())))
            }
        } catch (e: Exception) {
            emit(Result.Error(Result.ErrorType.DatabaseError(e)))
        }
    }.flowOn(Dispatchers.IO)
}

class BookmarkNotFoundException : Exception("Bookmark not found in local database")