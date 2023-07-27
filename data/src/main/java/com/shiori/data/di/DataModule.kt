package com.shiori.data.di

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.shiori.common.result.ErrorHandler
import com.shiori.data.local.datastore.UserPreferencesSerializer
import com.shiori.data.local.preferences.SettingsPreferenceDataSource
import com.shiori.data.local.preferences.SettingsPreferencesDataSourceImpl
import com.shiori.data.repository.AccountRepository
import com.shiori.data.repository.AccountRepositoryImpl
import com.shiori.data.repository.BookmarksRepository
import com.shiori.data.repository.BookmarksRepositoryImpl
import com.shiori.data.repository.AuthRepository
import com.shiori.data.repository.AuthRepositoryImpl
import com.shiori.data.repository.ErrorHandlerImpl
import com.shiori.data.repository.SettingsRepository
import com.shiori.data.repository.SettingsRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun dataModule() = module {

    single {
        PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            produceFile = { androidContext().preferencesDataStoreFile("user_data") }
        )
    }

    single {
        DataStoreFactory.create(
            serializer = UserPreferencesSerializer,
            produceFile = { androidContext().preferencesDataStoreFile("objects_data")},
            corruptionHandler = null,
            )
    }

    single { SettingsPreferencesDataSourceImpl(
        dataStore = get(),
        protoDataStore = get()
    ) as SettingsPreferenceDataSource }

    single { AuthRepositoryImpl(
        apiService = get(),
        settingsPreferenceDataSource = get(),
        errorHandler = get()
    ) as AuthRepository }

    single { SettingsRepositoryImpl(
        settingsPreferenceDataSource = get()
    ) as SettingsRepository }

    single { BookmarksRepositoryImpl(
        apiService = get(),
        bookmarksDao = get(),
        errorHandler = get()
    ) as BookmarksRepository }

    single { AccountRepositoryImpl(
        apiService = get(),
    ) as AccountRepository  }

    single { ErrorHandlerImpl() as ErrorHandler }

}