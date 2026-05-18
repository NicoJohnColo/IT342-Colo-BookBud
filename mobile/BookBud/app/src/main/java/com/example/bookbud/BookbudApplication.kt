package com.example.bookbud

import android.app.Application
import com.example.bookbud.shared.storage.PreferencesManager
import com.example.bookbud.shared.auth.TokenManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BookbudApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize storage singletons
        PreferencesManager.init(this)
        TokenManager.init(this)
    }
}
