package com.desarrollodroide.network.retrofit

import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.desarrollodroide.common.result.ErrorHandler
import kotlinx.coroutines.flow.*
import retrofit2.Response
import com.desarrollodroide.common.result.Result
import kotlin.coroutines.cancellation.CancellationException

/**
 * A generic class that can provide a resource backed by both the sqlite database and the network.
 *
 * Adapted from: Guide to app architecture
 * https://developer.android.com/jetpack/guide
 *
 * @param <ResultType> Represents the domain model
 * @param <RequestType> Represents the (converted) network > database model
 */
abstract class NetworkBoundResource<RequestType, ResultType>(
    private val errorHandler: ErrorHandler,
) {
    fun asFlow() = flow {
        emit(Result.Loading(null)) // start loading state immediately
        val cachedData = fetchFromLocal().firstOrNull()

        try {
            if (shouldFetch(cachedData)) {
                emit(Result.Loading(cachedData)) // update loading state with cached data

                val apiResponse = fetchFromRemote()
                val remoteResponse = apiResponse.body()

                if (apiResponse.isSuccessful && remoteResponse != null) {
                    saveRemoteData(remoteResponse)

                    // Collects all the values from the given flow and emits them to the collector
                    // Always fetch from local (Source of truth)
                    emitAll(fetchFromLocal().map { Result.Success(it) })
                } else {
                    emitAll(fetchFromLocal().map {
                        val message = apiResponse.errorBody()?.string()
                        Result.Error(errorHandler.getApiError(
                            statusCode = apiResponse.code(),
                            throwable = null,
                            message = message), it )
                    })
                }
            } else {
                emit(Result.Success(cachedData))
            }
        } catch (e: Exception) {
            //Error: StandaloneCoroutine was cancelled
            if (e !is CancellationException) {
                Log.v("NetworkBoundResource", "Error: ${e.message}")
                emitAll(fetchFromLocal().map { Result.Error(errorHandler.getError(e), it) })
            }
        }
    }

    @WorkerThread
    protected abstract suspend fun saveRemoteData(response: RequestType)

    @MainThread
    protected abstract fun fetchFromLocal(): Flow<ResultType>

    @MainThread
    protected abstract suspend fun fetchFromRemote(): Response<RequestType>

    @MainThread
    protected abstract fun shouldFetch(data: ResultType?): Boolean
}

abstract class NetworkNoCacheResource<RequestType, ResultType>(
    private val errorHandler: ErrorHandler,
) {
    fun asFlow() = flow {
        emit(Result.Loading(null)) // start loading state immediately

        try {
            val apiResponse = fetchFromRemote()
            val remoteResponse = apiResponse.body()
            if (apiResponse.isSuccessful && remoteResponse != null) {
                emitAll(fetchResult(remoteResponse).map { Result.Success(it) })
            } else {
                emit(Result.Error(errorHandler.getApiError(apiResponse.code()), null))
            }
        } catch (e: Exception) {
            emit(Result.Error(errorHandler.getError(e), null))
        }
    }

    @MainThread
    protected abstract suspend fun fetchFromRemote(): Response<RequestType>
    @MainThread
    protected abstract fun fetchResult(data: RequestType): Flow<ResultType>
}