package com.shiori.common.result

sealed class Result<out T>(
    val data: T? = null,
    val error: ErrorType? = null
) {
    class Success<T>(data: T) : Result<T>(data)
    class Loading<T>(data: T? = null) : Result<T>(data)
    class Error<T>(error: ErrorType? = null, data: T? = null) : Result<T>(data, error)

    sealed class ErrorType(
        val throwable: Throwable? = null,
        val statusCode: Int? = null,
        val message: String? = null
    ) {
        class DatabaseError(throwable: Throwable? = null) : ErrorType(throwable)
        class IOError(throwable: Throwable? = null) : ErrorType(throwable)
        class HttpError(throwable: Throwable? = null, statusCode: Int, message: String? = null) : ErrorType(
            throwable =  throwable,
            statusCode = statusCode,
            message = message
        )
        class Unknown(throwable: Throwable? = null) : ErrorType(throwable)
        class SessionExpired(throwable: Throwable? = null, message: String? = null) : ErrorType(throwable, message = message)
    }
}
