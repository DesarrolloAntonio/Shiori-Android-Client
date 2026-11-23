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

  /**
   * Whether the server at [serverUrl] takes this session as [userName]'s: the proof that a new
   * address reaches the same Shiori, since queued changes name bookmarks by that server's ids.
   */
  suspend fun checkSession(
    serverUrl: String,
    token: String,
    userName: String,
  ): SessionCheck
}

/** What a server said to the stored session. Only [Accepted] proves it is the same Shiori. */
sealed interface SessionCheck {
  data object Accepted : SessionCheck

  /** It answered, and not with this account: another server, or a session that has expired. */
  data object Refused : SessionCheck

  /** Something answered, but not the way Shiori does. */
  data object NotShiori : SessionCheck

  /** Nothing usable came back. */
  data class Unreachable(val reason: String) : SessionCheck
}