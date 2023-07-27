package com.shiori.data.repository

import com.shiori.common.result.Result
import com.shiori.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

  fun sendLogin(
    username: String,
    password: String,
    serverUrl: String
  ): Flow<Result<User?>>

  fun sendLogout(
    serverUrl: String,
    xSession: String
  ): Flow<Result<String?>>

}