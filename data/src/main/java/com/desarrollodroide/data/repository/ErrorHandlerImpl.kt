package com.desarrollodroide.data.repository

import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.helpers.SESSION_HAS_BEEN_EXPIRED
import java.io.IOException
import java.sql.SQLException

class ErrorHandlerImpl : ErrorHandler {
    override fun getError(throwable: Throwable): Result.ErrorType {
        return when (throwable) {
            is IOException -> Result.ErrorType.IOError(throwable)
            is SQLException -> Result.ErrorType.DatabaseError(throwable)
            else -> Result.ErrorType.Unknown(throwable)
        }
    }

    override fun getApiError(
        statusCode: Int,
        throwable: Throwable?,
        message: String?
    ): Result.ErrorType {
        return if (message?.contains(SESSION_HAS_BEEN_EXPIRED) == true)
            Result.ErrorType.SessionExpired(throwable, message) else
            Result.ErrorType.HttpError(throwable, statusCode, message)
    }
}