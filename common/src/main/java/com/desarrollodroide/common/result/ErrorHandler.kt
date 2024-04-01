package com.desarrollodroide.common.result

/**
 * Defines a contract for handling errors that may occur during the application's operations.
 * Allows obtaining a specific [Result.ErrorType] based on the error or API status code.
 */
interface ErrorHandler {
    /**
     * Returns an [Result.ErrorType] based on the given throwable.
     *
     * @param throwable The throwable that caused the error.
     * @return The specific [Result.ErrorType] that represents the error.
     */
    fun getError(throwable: Throwable): Result.ErrorType

    /**
     * Returns an [Result.ErrorType] for API errors based on the status code, optional throwable, and message.
     *
     * @param statusCode The HTTP status code of the API error.
     * @param throwable Optional throwable that may have caused the API error.
     * @param message Optional message describing the API error.
     * @return The specific [Result.ErrorType] that represents the API error.
     */
    fun getApiError(statusCode: Int, throwable: Throwable? = null, message: String? = null): Result.ErrorType
}
