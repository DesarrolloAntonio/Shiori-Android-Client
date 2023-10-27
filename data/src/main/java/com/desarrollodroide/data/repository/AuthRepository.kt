package com.desarrollodroide.data.repository

import com.desarrollodroide.common.result.Result
import com.desarrollodroide.model.User
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