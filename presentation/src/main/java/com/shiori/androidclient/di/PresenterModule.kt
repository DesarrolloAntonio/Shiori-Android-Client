package com.shiori.androidclient.di

import com.shiori.androidclient.ui.login.LoginViewModel
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

fun presenterModule() = module {

    viewModel {
        LoginViewModel(
//            getBookmarksUseCase = get(),
            loginUseCase = get(),
            userRepository = get()
        )
    }

}