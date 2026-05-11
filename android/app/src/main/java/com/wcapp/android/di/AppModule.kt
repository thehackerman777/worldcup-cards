package com.wcapp.android.di

import com.wcapp.android.data.local.SessionManager
import com.wcapp.android.data.remote.ApiService
import com.wcapp.android.security.SecurePrefs
import com.wcapp.android.ui.screens.album.AlbumViewModel
import com.wcapp.android.ui.screens.auth.AuthViewModel
import com.wcapp.android.ui.screens.cards.CardsViewModel
import com.wcapp.android.ui.screens.exchange.ExchangeViewModel
import com.wcapp.android.ui.screens.home.HomeViewModel
import com.wcapp.android.ui.screens.panini.PaniniViewModel
import com.wcapp.android.ui.screens.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Security
    single { SecurePrefs(androidContext()) }

    // Session manager
    single { SessionManager(get()) }

    // API Service
    single { ApiService(get()) }

    // ViewModels
    factory { AuthViewModel(get(), get()) }
    factory { HomeViewModel(get(), get()) }
    factory { CardsViewModel(get()) }
    factory { AlbumViewModel(get()) }
    factory { ExchangeViewModel(get()) }
    factory { SettingsViewModel(get()) }
    factory { PaniniViewModel(get()) }
}
