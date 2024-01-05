package com.desarrollodroide.data.repository

import android.util.Log
import com.desarrollodroide.data.mapper.toDomainModel
import com.desarrollodroide.model.Account
import com.desarrollodroide.network.model.AccountDTO
import com.desarrollodroide.network.model.ApiResponse
//import com.shiori.network.model.state.ErrorHandler
//import com.shiori.network.model.state.Result
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

class AccountRepositoryImpl(
    private val apiService: RetrofitNetwork,
) : AccountRepository {

    override fun createAccount(
        xSession: String,
        userName: String,
        password: String
    ): Flow<ApiResponse<out Account>> {
        return flow {
            try {
                val result = apiService.createAccount(
                    xSessionId = xSession,
                    account = AccountDTO(
                        userName = userName,
                        password = password,
                        isOwner = false
                    )
                )
                emit(handleApiResponse(result) { it.toDomainModel() })
            } catch (exception: Exception) {
                emit(handleApiException(exception))
            }
        }.flowOn(Dispatchers.IO)
    }
}
fun <T, R> handleApiResponse(
    response: Response<T>,
    transform: (T) -> R
): ApiResponse<R> {
    return if (response.isSuccessful && response.body() != null) {
        ApiResponse(success = true, data = transform(response.body()!!))
    } else {
        if (response.errorBody()?.charStream() != null) {
            ApiResponse(success = false, error = response.errorBody()?.charStream()?.readText()?:"")
        } else {
            ApiResponse(success = false, error = "Unknown error")
        }
    }
}

fun handleApiException(exception: Exception): ApiResponse<Nothing> {
    Log.v("Error!!", exception.message ?: "")
    return ApiResponse(success = false, error = "Error: ${exception.message}")
}


