package com.shiori.data.di

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.shiori.data.local.preferences.UserPreferencesImpl
import com.shiori.data.local.preferences.UserPreference
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

    single { UserPreferencesImpl(get()) as UserPreference }

}