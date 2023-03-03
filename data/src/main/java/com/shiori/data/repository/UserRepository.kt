package com.shiori.data.repository

import com.shiori.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun getUser(): Flow<User>
    suspend fun getUserName(): Flow<String>

    val userDataStream: Flow<User>
}