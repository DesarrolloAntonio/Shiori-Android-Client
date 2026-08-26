package com.desarrollodroide.data.repository

import com.desarrollodroide.common.result.Result
import com.desarrollodroide.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

  fun sendLogout(
    serverUrl: String,
    xSession: String
  ): Flow<Result<String?>>

  /**
   * Exchanges a still-valid token for a new one with a fresh expiry.
   * Emits the new token, which has already been persisted by the time it arrives.
   */
  fun refreshToken(
    serverUrl: String,
    token: String
  ): Flow<Result<String?>>

  fun sendLoginV1(
    username: String,
    password: String,
    serverUrl: String
  ): Flow<Result<User?>>
}