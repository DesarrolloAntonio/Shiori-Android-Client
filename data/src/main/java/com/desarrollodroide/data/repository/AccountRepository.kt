package com.desarrollodroide.data.repository

import com.desarrollodroide.model.Account
import com.desarrollodroide.network.model.ApiResponse
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    fun createAccount(
        xSession: String,
        userName: String,
        password: String
    ): Flow<ApiResponse<out Account>>


}