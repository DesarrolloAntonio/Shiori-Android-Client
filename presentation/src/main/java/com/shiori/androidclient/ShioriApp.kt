package com.shiori.androidclient

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ShioriApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ShioriApplication)
            modules(
                listOf(
//                    presenterModule(),
//                    appModule(),
//                    networkingModule()
                )
            )
        }
    }
}