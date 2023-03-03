package com.shiori.data.repository

import com.shiori.data.local.preferences.UserPreferenceDataSource
import com.shiori.model.Account
import com.shiori.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val userPreferenceDataSource: UserPreferenceDataSource
): UserRepository {
    override suspend fun getUser() = userPreferenceDataSource.userDataStream.map {
        User(
            session = it.session,
            account = Account(
                id = it.account.id,
                userName = it.account.userName,
                password = it.account.password,
                owner = it.account.owner
            )
        )
    }

//    override suspend fun saveUser(name: String) {
//        val session = UserPreferences.newBuilder()
//            .setUsername(name)
//            .build()
//        userPreferenceDataSource.saveUser(session = session)
//    }

    override suspend fun getUserName() = userPreferenceDataSource.userDataStream.map { it.account.userName }

    override val userDataStream: Flow<User> =
        userPreferenceDataSource.userDataStream
}