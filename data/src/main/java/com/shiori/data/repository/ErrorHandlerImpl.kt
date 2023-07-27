package com.shiori.data.repository

import com.shiori.common.result.ErrorHandler
import com.shiori.common.result.Result
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
        return if (message?.contains("session has been expired") == true)
            Result.ErrorType.SessionExpired(throwable, message) else
            Result.ErrorType.HttpError(throwable, statusCode, message)
    }
}