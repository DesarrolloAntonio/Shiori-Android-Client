package com.desarrollodroide.domain.usecase

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