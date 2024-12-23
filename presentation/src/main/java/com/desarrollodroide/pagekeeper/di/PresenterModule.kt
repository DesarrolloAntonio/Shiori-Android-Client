package com.desarrollodroide.pagekeeper.di

import com.desarrollodroide.pagekeeper.ui.feed.FeedViewModel
import com.desarrollodroide.pagekeeper.ui.login.LoginViewModel
import com.desarrollodroide.pagekeeper.ui.bookmarkeditor.BookmarkViewModel
import com.desarrollodroide.pagekeeper.ui.feed.SearchViewModel
import com.desarrollodroide.pagekeeper.ui.readablecontent.ReadableContentViewModel
import com.desarrollodroide.pagekeeper.ui.settings.SettingsViewModel
import com.desarrollodroide.pagekeeper.ui.settings.crash.CrashLogViewModel
import com.desarrollodroide.pagekeeper.ui.settings.logcat.NetworkLogViewModel
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

fun presenterModule() = module {

    viewModel {
        LoginViewModel(
            loginUseCase = get(),
            settingsPreferenceDataSource = get(),
            livenessUseCase = get(),
        )
    }

    viewModel {
        FeedViewModel(
            bookmarkDatabase = get(),
            settingsPreferenceDataSource = get(),
            getTagsUseCase = get(),
            deleteBookmarkUseCase = get(),
            updateBookmarkCacheUseCase = get(),
            getLocalPagingBookmarksUseCase = get(),
            downloadFileUseCase = get(),
            getAllRemoteBookmarksUseCase = get(),
            deleteLocalBookmarkUseCase = get(),
            syncBookmarksUseCase = get(),
            syncManager = get(),
        )
    }

    viewModel {
        SettingsViewModel(
            settingsPreferenceDataSource = get(),
            bookmarksRepository = get(),
            sendLogoutUseCase = get(),
            themeManager = get(),
            getTagsUseCase = get(),
            imageLoader = get(),
        )
    }

    viewModel {
        BookmarkViewModel(
            bookmarkDatabase = get(),
            bookmarksRepository = get(),
            bookmarkAdditionUseCase = get(),
            editBookmarkUseCase = get(),
            userPreferences = get(),
            settingsPreferenceDataSource =  get(),
        )
    }

    viewModel {
        SearchViewModel(
            getPagingBookmarksUseCase = get(),
            settingsPreferenceDataSource = get(),
        )
    }

    viewModel {
        ReadableContentViewModel(
            getBookmarkReadableContentUseCase = get(),
            settingsPreferenceDataSource = get(),
            bookmarksDao = get(),
            bookmarkHtmlDao = get(),
        )
    }

    viewModel {
        NetworkLogViewModel(
            logger = get(),
        )
    }

    viewModel {
        CrashLogViewModel(
            settingsPreferenceDataSource = get(),
        )
    }

}