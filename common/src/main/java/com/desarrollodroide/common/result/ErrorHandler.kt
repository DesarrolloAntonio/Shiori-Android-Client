package com.desarrollodroide.common.result

interface ErrorHandler {
    fun getError(throwable: Throwable): Result.ErrorType
    fun getApiError(statusCode: Int, throwable: Throwable? = null, message: String? = null): Result.ErrorType

}