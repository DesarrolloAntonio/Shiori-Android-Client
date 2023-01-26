package com.shiori.domain.repository

import com.shiori.domain.model.Bookmark
import com.shiori.domain.model.User
import kotlinx.coroutines.flow.Flow
import com.shiori.domain.model.state.Result

interface MainRepository {

  fun getBookmarks(): Flow<Result<out List<Bookmark>?>>

  suspend  fun getUser(): Flow<User>
  suspend fun saveUser(userName: String, password: String)

  suspend fun storeUrl(url: String)
  suspend fun storeUser(user: User)
}