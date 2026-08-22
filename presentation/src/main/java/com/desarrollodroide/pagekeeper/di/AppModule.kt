package com.desarrollodroide.pagekeeper.di

import android.content.Context
import android.util.Log
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import okio.Path.Companion.toOkioPath
import com.desarrollodroide.pagekeeper.helpers.ThemeManager
import com.desarrollodroide.pagekeeper.helpers.ThemeManagerImpl
import com.desarrollodroide.data.repository.BookmarksRepository
import com.desarrollodroide.data.repository.BookmarksRepositoryImpl
import com.desarrollodroide.domain.usecase.AddBookmarkUseCase
import com.desarrollodroide.domain.usecase.DeleteBookmarkUseCase
import com.desarrollodroide.domain.usecase.DeleteLocalBookmarkUseCase
import com.desarrollodroide.domain.usecase.DownloadFileUseCase
import com.desarrollodroide.domain.usecase.EditBookmarkUseCase
import com.desarrollodroide.domain.usecase.GetBookmarkReadableContentUseCase
import com.desarrollodroide.domain.usecase.GetBookmarksUseCase
import com.desarrollodroide.domain.usecase.GetLocalPagingBookmarksUseCase
import com.desarrollodroide.domain.usecase.GetTagsUseCase
import com.desarrollodroide.domain.usecase.CreateTagUseCase
import com.desarrollodroide.domain.usecase.DeleteTagUseCase
import com.desarrollodroide.domain.usecase.RefreshTokenUseCase
import com.desarrollodroide.domain.usecase.RenameTagUseCase
import com.desarrollodroide.domain.usecase.SendLoginUseCase
import com.desarrollodroide.domain.usecase.SendLogoutUseCase
import com.desarrollodroide.domain.usecase.GetAllRemoteBookmarksUseCase
import com.desarrollodroide.domain.usecase.SystemLivenessUseCase
import com.desarrollodroide.domain.usecase.UpdateBookmarkCacheUseCase
import okhttp3.OkHttpClient
import org.koin.dsl.module

fun appModule() = module {

    single {
        BookmarksRepositoryImpl(
            apiService = get(),
            bookmarksDao = get(),
            tagDao = get(),
            errorHandler = get(),
        ) as BookmarksRepository
    }

    single {
        GetBookmarksUseCase(
            bookmarksRepository = get()
        )
    }

    single {
        GetLocalPagingBookmarksUseCase(
            bookmarksRepository = get()
        )
    }

    single {
        DeleteBookmarkUseCase(
            bookmarksDao = get(),
            syncManager = get()
        )
    }

    single {
        DeleteLocalBookmarkUseCase(
            bookmarksDao = get()
        )
    }

    single {
        SendLoginUseCase(
            authRepository = get()
        )
    }

    single {
        SendLogoutUseCase(
            authRepository = get(),
            syncManager = get(),
            settingsPreferenceDataSource = get(),
            bookmarksRepository = get()
        )
    }

    single {
        AddBookmarkUseCase(
            bookmarksDao = get(),
            syncManager = get()
        )
    }

    single {
        EditBookmarkUseCase(
            bookmarksDao = get(),
            tagsDao = get(),
            syncManager = get()
        )
    }

    single {
        UpdateBookmarkCacheUseCase(
            bookmarksDao = get(),
            syncManager = get()
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
        CreateTagUseCase(
            tagsRepository = get()
        )
    }

    single {
        RenameTagUseCase(
            tagsRepository = get()
        )
    }

    single {
        DeleteTagUseCase(
            tagsRepository = get()
        )
    }

    single {
        RefreshTokenUseCase(
            authRepository = get()
        )
    }

    single {
        GetAllRemoteBookmarksUseCase(
            bookmarksRepository = get()
        )
    }

    single { ThemeManagerImpl(get()) as ThemeManager }

    single {
        ImageLoader.Builder(get<Context>())
            // coil3 has no okHttpClient {}. The network layer is a fetcher component, and the
            // okhttp one has to be added explicitly or nothing over http loads at all.
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = {
                    OkHttpClient.Builder()
                        .retryOnConnectionFailure(true)
                        .addInterceptor { chain ->
                            val request = chain.request()
                            val response = chain.proceed(request)
                            if (!response.isSuccessful) {
                                Log.e("BookmarkImageView", "HTTP error: ${response.code}")
                            }
                            val newCacheControl = "public, max-age=31536000"
                            response.newBuilder()
                                .header("Cache-Control", newCacheControl)
                                .build()
                        }
                        .build()
                }))
            }
            .diskCache {
                DiskCache.Builder()
                    // okio Path, not File.
                    .directory(get<Context>().cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(250L * 1024 * 1024) // 250MB
                    .build()
            }
            .build()
    }

}