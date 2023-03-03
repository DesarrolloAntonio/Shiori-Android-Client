package com.shiori.data.di

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.shiori.data.local.datastore.UserPreferencesSerializer
import com.shiori.data.local.preferences.UserPreferenceDataSource
import com.shiori.data.local.preferences.UserPreferencesDataSourceImpl
import com.shiori.data.repository.MainRepository
import com.shiori.data.repository.MainRepositoryImpl
import com.shiori.data.repository.UserRepository
import com.shiori.data.repository.UserRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

fun preferencesModule() = module {

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

    single { UserPreferencesDataSourceImpl(
        dataStore = get(),
        protoDataStore = get()
    ) as UserPreferenceDataSource }

    single { MainRepositoryImpl(
        apiService = get(),
        userPreferenceDataSource = get()
    ) as MainRepository }
    single { UserRepositoryImpl(
        userPreferenceDataSource = get()
    ) as UserRepository }

}