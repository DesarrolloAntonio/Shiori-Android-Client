package com.shiori.androidclient.di

import com.shiori.androidclient.ui.feed.FeedViewModel
import com.shiori.androidclient.ui.login.LoginViewModel
import com.shiori.androidclient.ui.bookmarkeditor.BookmarkViewModel
import com.shiori.androidclient.ui.settings.SettingsViewModel
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

fun presenterModule() = module {

    viewModel {
        LoginViewModel(
            loginUseCase = get(),
            settingsPreferenceDataSource = get(),
        )
    }

    viewModel {
        FeedViewModel(
            settingsPreferenceDataSource = get(),
            getBookmarksUseCase = get(),
            deleteBookmarkUseCase = get(),
        )
    }

    viewModel {
        SettingsViewModel(
            settingsPreferenceDataSource = get(),
            sendLogoutUseCase = get(),
            themeManager = get(),
        )
    }

    viewModel {
        BookmarkViewModel(
            bookmarkDatabase = get(),
            bookmarkAdditionUseCase = get(),
            editBookmarkUseCase = get(),
            userPreferences = get(),
        )
    }

}