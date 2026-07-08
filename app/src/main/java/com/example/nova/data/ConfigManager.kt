package com.example.nova.data

import android.content.Context
import android.content.SharedPreferences
import com.example.nova.BuildConfig

class ConfigManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("nova_config", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_JELLYSEER_URL = "jellyseer_url"
    }

    // Jellyseer URL (user configures in settings)
    fun setJellyseerUrl(url: String) {
        sharedPreferences.edit().putString(KEY_JELLYSEER_URL, url).apply()
    }

    fun getJellyseerUrl(): String? = sharedPreferences.getString(KEY_JELLYSEER_URL, null)

    // API Key (built-in from gradle.properties)
    fun getJellyseerApiKey(): String {
        return try {
            BuildConfig.JELLYSEER_API_KEY
        } catch (e: Exception) {
            "" // Fallback if not configured
        }
    }

    fun isJellyseerConfigured(): Boolean {
        return !getJellyseerUrl().isNullOrBlank() && getJellyseerApiKey().isNotBlank()
    }
}