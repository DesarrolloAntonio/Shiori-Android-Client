package com.desarrollodroide.common.result
/**
 * Represents the outcome of an operation that can end in success, failure, or be in progress.
 * It is a sealed class that can take one of the following forms:
 * - Success: Indicates the operation was successful.
 * - Loading: Indicates the operation is in progress.
 * - Error: Indicates the operation failed.
 *
 * @param T The expected data type in case of success.
 * @param data The resulting data in case of success. Null if the operation was not successful.
 * @param error The error that occurred if the operation failed.
 */
sealed class Result<out T>(
    val data: T? = null,
    val error: ErrorType? = null
) {
    class Success<T>(data: T) : Result<T>(data)
    class Loading<T>(data: T? = null) : Result<T>(data)
    class Error<T>(error: ErrorType? = null, data: T? = null) : Result<T>(data, error)

    /**
     * Represents various error types that can occur.
     * Includes:
     * - DatabaseError: For errors related to database operations.
     * - IOError: For input/output operation failures.
     * - HttpError: For HTTP request failures, with status code and optional message.
     * - Unknown: For undetermined errors.
     * - SessionExpired: Specifically for session expiration errors.
     */
    sealed class ErrorType(
        val throwable: Throwable? = null,
        val statusCode: Int? = null,
        val message: String? = null
    ) {
        class DatabaseError(throwable: Throwable? = null) : ErrorType(throwable)
        class IOError(throwable: Throwable? = null) : ErrorType(throwable)
        class HttpError(throwable: Throwable? = null, statusCode: Int, message: String? = null) : ErrorType(throwable, statusCode, message)
        class Unknown(throwable: Throwable? = null) : ErrorType(throwable)
        class SessionExpired(throwable: Throwable? = null, message: String? = null) : ErrorType(throwable, message = message)
    }
}
