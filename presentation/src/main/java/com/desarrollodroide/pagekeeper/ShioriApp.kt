package com.desarrollodroide.pagekeeper

import android.app.Application
import com.desarrollodroide.pagekeeper.di.presenterModule
import com.desarrollodroide.pagekeeper.di.appModule
import com.desarrollodroide.data.di.dataModule
import com.desarrollodroide.data.di.databaseModule
import com.desarrollodroide.network.di.networkingModule
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
    }
}

