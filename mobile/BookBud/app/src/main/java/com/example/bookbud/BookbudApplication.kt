package com.example.bookbud

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BookbudApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Hilt takes over dependency injection
    }
}
