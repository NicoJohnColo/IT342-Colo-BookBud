package com.example.bookbud.shared.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch

object PreferencesManager {
    private const val PREFS_NAME = "bookbud_prefs"
    private const val TOKEN_KEY = "access_token"
    private const val USER_KEY = "user"
    
    private lateinit var context: Context
    private var _prefs: SharedPreferences? = null
    private var initLatch: CountDownLatch? = null
    
    fun init(context: Context) {
        this.context = context
        
        // Initialize on background thread to avoid blocking main thread
        initLatch = CountDownLatch(1)
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                
                _prefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } finally {
                initLatch?.countDown()
            }
        }
    }
    
    private fun getPrefs(): SharedPreferences? {
        // Wait for initialization to complete (max 5 seconds timeout)
        if (_prefs == null && initLatch != null) {
            try {
                initLatch?.await()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        return _prefs
    }
    
    fun saveToken(token: String) {
        getPrefs()?.edit()?.putString(TOKEN_KEY, token)?.apply()
    }
    
    fun getToken(): String? = getPrefs()?.getString(TOKEN_KEY, null)
    
    fun clearAll() {
        getPrefs()?.edit()?.clear()?.apply()
    }
}

