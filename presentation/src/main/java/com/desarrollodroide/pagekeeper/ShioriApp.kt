package com.desarrollodroide.pagekeeper

import android.app.Application
import coil.ImageLoader
import com.desarrollodroide.pagekeeper.di.presenterModule
import com.desarrollodroide.pagekeeper.di.appModule
import com.desarrollodroide.data.di.dataModule
import com.desarrollodroide.data.di.databaseModule
import com.desarrollodroide.data.helpers.CrashHandler
import com.desarrollodroide.network.di.networkingModule
import com.desarrollodroide.pagekeeper.extensions.logCacheDetails
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ShioriApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ShioriApplication)
            modules(
                listOf(
                    networkingModule(),
                    appModule(),
                    presenterModule(),
                    dataModule(),
                    databaseModule()
                )
            )
        }
        // Show log disk cache statistics for debugging
        val imageLoader: ImageLoader by inject()
        imageLoader.logCacheDetails()

        val crashHandler: CrashHandler by inject()
        crashHandler.initialize()

    }
}

