package com.shiori.data.repository

import com.shiori.model.Bookmark
import com.shiori.model.User
import kotlinx.coroutines.flow.Flow

interface MainRepository {

  fun getBookmarks(): Flow<List<Bookmark>>
//  suspend fun sendLogin(account: Account, serverUrl: String): Flow<Result<out UserSession?>>
//  suspend  fun getUser(): Flow<Session>
//  suspend fun saveUser(session: Session)
//  suspend fun storeUrl(url: String)
//  suspend fun storeUser(user: Account)

  fun sendLogin(
    username: String,
    password: String,
    serverUrl: String
  ): Flow<User?>
}