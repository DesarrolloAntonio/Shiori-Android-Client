package com.shiori.data.repository

import com.shiori.model.Account
import com.shiori.network.model.AccountDTO
import com.shiori.network.model.ApiResponse
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    fun createAccount(
        xSession: String,
        userName: String,
        password: String
    ): Flow<ApiResponse<out Account>>


}