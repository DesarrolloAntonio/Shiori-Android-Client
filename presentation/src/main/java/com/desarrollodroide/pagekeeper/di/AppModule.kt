package com.desarrollodroide.pagekeeper.di

import com.desarrollodroide.pagekeeper.helpers.ThemeManager
import com.desarrollodroide.pagekeeper.helpers.ThemeManagerImpl
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.data.repository.BookmarksRepositoryImpl
import com.desarrollodroide.domain.usecase.AddBookmarkUseCase
import com.desarrollodroide.domain.usecase.DeleteBookmarkUseCase
import com.desarrollodroide.domain.usecase.DownloadFileUseCase
import com.desarrollodroide.domain.usecase.EditBookmarkUseCase
import com.desarrollodroide.domain.usecase.GetBookmarkReadableContentUseCase
import com.desarrollodroide.domain.usecase.GetBookmarksUseCase
import com.desarrollodroide.domain.usecase.GetPagingBookmarksUseCase
import com.desarrollodroide.domain.usecase.GetTagsUseCase
import com.desarrollodroide.domain.usecase.SendLoginUseCase
import com.desarrollodroide.domain.usecase.SendLogoutUseCase
import com.desarrollodroide.domain.usecase.SyncInitialBookmarksUseCase
import com.desarrollodroide.domain.usecase.SystemLivenessUseCase
import com.desarrollodroide.domain.usecase.UpdateBookmarkCacheUseCase
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
        GetPagingBookmarksUseCase(
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

    single {
        EditBookmarkUseCase(
            bookmarksRepository = get()
        )
    }

    single {
        UpdateBookmarkCacheUseCase(
            bookmarksRepository = get()
        )
    }

    single {
        DownloadFileUseCase(
            fileRepository = get()
        )
    }

    single {
        SystemLivenessUseCase(
            systemRepository = get()
        )
    }

    single {
        GetTagsUseCase(
            tagsRepository = get()
        )
    }

    single {
        GetBookmarkReadableContentUseCase(
            bookmarksRepository = get()
        )
    }

    single {
        SyncInitialBookmarksUseCase(
            bookmarksRepository = get()
        )
    }

    single { ThemeManagerImpl(get()) as ThemeManager }


}