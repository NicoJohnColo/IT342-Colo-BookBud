package com.example.bookbud.shared.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.bookbud.shared.models.AuthResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch

object TokenManager {
    private const val PREFS_NAME = "bookbud_auth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_ROLE = "role"

    private var prefs: SharedPreferences? = null
    private val latch = CountDownLatch(1)

    fun init(context: Context) {
        if (prefs != null) return
        
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                
                prefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } finally {
                latch.countDown()
            }
        }
    }

    fun saveAuthResponse(response: AuthResponse) {
        ensureInitialized()
        prefs?.edit()?.apply {
            putString(KEY_ACCESS_TOKEN, response.accessToken)
            putString(KEY_REFRESH_TOKEN, response.refreshToken)
            putString(KEY_USER_ID, response.userId)
            putString(KEY_USERNAME, response.username)
            putString(KEY_EMAIL, response.email)
            putString(KEY_ROLE, response.role ?: "USER")
            apply()
        }
    }

    fun getAccessToken(): String? {
        ensureInitialized()
        return prefs?.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        ensureInitialized()
        return prefs?.getString(KEY_REFRESH_TOKEN, null)
    }

    fun getUserId(): String? {
        ensureInitialized()
        return prefs?.getString(KEY_USER_ID, null)
    }

    fun getUsername(): String? {
        ensureInitialized()
        return prefs?.getString(KEY_USERNAME, null)
    }

    fun getEmail(): String? {
        ensureInitialized()
        return prefs?.getString(KEY_EMAIL, null)
    }

    fun getRole(): String? {
        ensureInitialized()
        return prefs?.getString(KEY_ROLE, null)
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    fun clearAll() {
        ensureInitialized()
        prefs?.edit()?.clear()?.apply()
    }

    private fun ensureInitialized() {
        if (prefs == null) {
            try {
                latch.await()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }
}
