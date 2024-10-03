package com.desarrollodroide.pagekeeper.di

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.disk.DiskCache
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
import com.desarrollodroide.domain.usecase.SendLoginUseCase
import com.desarrollodroide.domain.usecase.SendLogoutUseCase
import com.desarrollodroide.domain.usecase.SyncBookmarksUseCase
import com.desarrollodroide.domain.usecase.SyncInitialBookmarksUseCase
import com.desarrollodroide.domain.usecase.SystemLivenessUseCase
import com.desarrollodroide.domain.usecase.UpdateBookmarkCacheUseCase
import okhttp3.OkHttpClient
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
        GetLocalPagingBookmarksUseCase(
            bookmarksRepository = get()
        )
    }

    single {
        DeleteBookmarkUseCase(
            bookmarksRepository = get()
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

    single {
        SyncBookmarksUseCase(
            bookmarksRepository = get()
        )
    }

    single { ThemeManagerImpl(get()) as ThemeManager }

    single {
        ImageLoader.Builder(get<Context>())
            .okHttpClient {
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
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(get<Context>().cacheDir.resolve("image_cache"))
                    .maxSizeBytes(250L * 1024 * 1024) // 250MB
                    .build()
            }
            .build()
    }

}