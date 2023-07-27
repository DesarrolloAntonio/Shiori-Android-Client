package com.shiori.androidclient

import android.app.Application
import com.shiori.androidclient.di.presenterModule
import com.shiori.androidclient.di.appModule
import com.shiori.data.di.dataModule
import com.shiori.data.di.databaseModule
import com.shiori.network.di.networkingModule
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