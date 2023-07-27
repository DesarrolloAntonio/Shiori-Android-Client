package com.shiori.domain.usecase

import com.shiori.data.repository.AccountRepository

import com.shiori.model.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

//class CreateAccountUseCase(
//    private val accountRepository: AccountRepository
//) {
//    suspend operator fun invoke(
//        serverUrl: String,
//        xSession: String,
//        userName: String,
//        password: String
//    ): Flow<Result<Account>> {
//        return accountRepository.createAccount(
//            xSession = xSession,
//            userName = userName,
//            password = password
//        )
//            .map { it.data!! }
//            .flowOn(Dispatchers.IO)
//    }
//}