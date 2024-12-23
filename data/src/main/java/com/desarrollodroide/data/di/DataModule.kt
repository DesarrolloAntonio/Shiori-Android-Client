package com.desarrollodroide.data.di

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.work.WorkManager
import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.data.helpers.CrashHandler
import com.desarrollodroide.data.helpers.CrashHandlerImpl
import com.desarrollodroide.data.local.datastore.HideTagSerializer
import com.desarrollodroide.data.local.datastore.RememberUserPreferencesSerializer
import com.desarrollodroide.data.local.datastore.SystemPreferencesSerializer
import com.desarrollodroide.data.local.datastore.UserPreferencesSerializer
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.local.preferences.SettingsPreferencesDataSourceImpl
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.data.repository.BookmarksRepositoryImpl
import com.desarrollodroide.data.repository.AuthRepository
import com.desarrollodroide.data.repository.AuthRepositoryImpl
import com.desarrollodroide.data.repository.ErrorHandlerImpl
import com.desarrollodroide.data.repository.FileRepository
import com.desarrollodroide.data.repository.FileRepositoryImpl
import com.desarrollodroide.data.repository.SettingsRepository
import com.desarrollodroide.data.repository.SettingsRepositoryImpl
import com.desarrollodroide.data.repository.SyncWorks
import com.desarrollodroide.data.repository.SyncWorksImpl
import com.desarrollodroide.data.repository.SystemRepository
import com.desarrollodroide.data.repository.SystemRepositoryImpl
import com.desarrollodroide.data.repository.TagsRepository
import com.desarrollodroide.data.repository.TagsRepositoryImpl
import com.desarrollodroide.data.repository.workers.SyncWorker
import com.desarrollodroide.network.retrofit.FileRemoteDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun dataModule() = module {

    val preferencesDataStoreQualifier = named("preferencesDataStore")
    val protoDataStoreQualifier = named("protoDataStore")
    val protoRememberUserDataStoreQualifier = named("protoRememberUserDataStore")
    val protoHideTagDataStoreQualifier = named("protoHideTagDataStore")
    val protoSystemDataStoreQualifier = named("protoSystemDataStore")

    single(preferencesDataStoreQualifier) {
        PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            produceFile = { androidContext().preferencesDataStoreFile("user_data") }
        )
    }

    single(protoDataStoreQualifier) {
        DataStoreFactory.create(
            serializer = UserPreferencesSerializer,
            produceFile = { androidContext().preferencesDataStoreFile("objects_data")},
            corruptionHandler = null,
        )
    }

    single(protoRememberUserDataStoreQualifier) {
        DataStoreFactory.create(
            serializer = RememberUserPreferencesSerializer,
            produceFile = { androidContext().preferencesDataStoreFile("remember_user_data")},
            corruptionHandler = null,
        )
    }

    single(protoHideTagDataStoreQualifier) {
        DataStoreFactory.create(
            serializer = HideTagSerializer,
            produceFile = { androidContext().preferencesDataStoreFile("hide_tag_data")},
            corruptionHandler = null,
        )
    }

    single(protoSystemDataStoreQualifier) {
        DataStoreFactory.create(
            serializer = SystemPreferencesSerializer,
            produceFile = { androidContext().preferencesDataStoreFile("system_data")},
            corruptionHandler = null,
        )
    }

    single { SettingsPreferencesDataSourceImpl(
        dataStore = get(preferencesDataStoreQualifier),
        protoDataStore = get(protoDataStoreQualifier),
        systemPreferences = get(protoSystemDataStoreQualifier),
        rememberUserProtoDataStore = get(protoRememberUserDataStoreQualifier),
        hideTagDataStore = get(protoHideTagDataStoreQualifier)
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

    single { FileRepositoryImpl(
        context = androidContext(),
        remoteDataSource = get(),
    ) as FileRepository }

    single {
        SystemRepositoryImpl(
            apiService = get(),
            settingsPreferenceDataSource = get(),
            errorHandler = get()
        ) as SystemRepository
    }

    single {
        TagsRepositoryImpl(
            apiService = get(),
            tagsDao = get(),
            errorHandler = get()
        ) as TagsRepository
    }

    single { FileRemoteDataSource() }
    single { ErrorHandlerImpl() as ErrorHandler }

    single { WorkManager.getInstance(get<Context>()) }
    single { SyncWorker.Factory() }

    single { SyncWorksImpl(
        workManager = get(),
        bookmarksDao = get(),
        ) as SyncWorks
    }

    single {
        CrashHandlerImpl(
            settingsPreferenceDataSource = get()
        ) as CrashHandler
    }

}