package com.shiori.data.di

import com.shiori.data.repository.MainRepositoryImpl
import com.shiori.domain.repository.MainRepository
import com.shiori.domain.usecase.GetBookmarksUseCase
import org.koin.dsl.module

fun appModule() = module {

    single { MainRepositoryImpl(get(), get()) as MainRepository }
    single { GetBookmarksUseCase(get()) }

}