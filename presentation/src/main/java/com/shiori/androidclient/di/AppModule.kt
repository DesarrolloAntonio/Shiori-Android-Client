package com.shiori.androidclient.di

import com.shiori.data.repository.MainRepository
import com.shiori.data.repository.MainRepositoryImpl
import com.shiori.domain.usecase.GetBookmarksUseCase
import com.shiori.domain.usecase.SendLoginUseCase
import org.koin.dsl.module
import retrofit2.Retrofit

fun appModule() = module {

    single {
        MainRepositoryImpl(
            apiService = get(),
            userPreferenceDataSource = get()
        ) as MainRepository
    }
    single {
        GetBookmarksUseCase(
            mainRepository = get()
        )
    }
    single {
        SendLoginUseCase(
            mainRepository = get()
        )
    }

}