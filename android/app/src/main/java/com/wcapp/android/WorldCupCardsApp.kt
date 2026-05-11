package com.wcapp.android

import android.app.Application
import com.wcapp.android.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WorldCupCardsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WorldCupCardsApp)
            modules(appModule)
        }
    }
}
