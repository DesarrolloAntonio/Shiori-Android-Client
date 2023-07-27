package com.shiori.androidclient.di

import com.shiori.data.repository.BookmarksRepository
import com.shiori.data.repository.BookmarksRepositoryImpl
import com.shiori.domain.usecase.AddBookmarkUseCase
import com.shiori.domain.usecase.DeleteBookmarkUseCase
import com.shiori.domain.usecase.GetBookmarksUseCase
import com.shiori.domain.usecase.SendLoginUseCase
import com.shiori.domain.usecase.SendLogoutUseCase
import org.koin.dsl.module

fun appModule() = module {


    single {
        BookmarksRepositoryImpl(
            apiService = get(),
            bookmarksDao = get(),
            errorHandler = get(),
        ) as BookmarksRepository
    }

    single {
        GetBookmarksUseCase(
            bookmarksRepository = get()
        )
    }

    single {
        DeleteBookmarkUseCase(
            bookmarksRepository = get()
        )
    }

    single {
        SendLoginUseCase(
            authRepository = get()
        )
    }

    single {
        SendLogoutUseCase(
            authRepository = get()
        )
    }

    single {
        AddBookmarkUseCase(
            bookmarksRepository = get()
        )
    }

}