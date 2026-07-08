package com.example.nova.data

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("nova_auth", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_SERVER_ID = "server_id"
    }

    // Save auth credentials after successful login
    fun saveCredentials(
        serverUrl: String,
        username: String,
        password: String,
        accessToken: String,
        userId: String,
        serverId: String
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_SERVER_URL, serverUrl)
            putString(KEY_USERNAME, username)
            putString(KEY_PASSWORD, password)
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_USER_ID, userId)
            putString(KEY_SERVER_ID, serverId)
            apply()
        }
    }

    // Get stored access token
    fun getAccessToken(): String? = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)

    // Get stored user ID
    fun getUserId(): String? = sharedPreferences.getString(KEY_USER_ID, null)

    // Get stored server URL
    fun getServerUrl(): String? = sharedPreferences.getString(KEY_SERVER_URL, null)

    // Get stored username
    fun getUsername(): String? = sharedPreferences.getString(KEY_USERNAME, null)

    // Get stored password
    fun getPassword(): String? = sharedPreferences.getString(KEY_PASSWORD, null)

    // Get stored server ID
    fun getServerId(): String? = sharedPreferences.getString(KEY_SERVER_ID, null)

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrBlank() && !getUserId().isNullOrBlank()
    }

    // Clear all credentials (logout)
    fun clearCredentials() {
        sharedPreferences.edit().clear().apply()
    }

    // Get all stored credentials (for auto-login)
    fun getStoredCredentials(): StoredCredentials? {
        val serverUrl = getServerUrl() ?: return null
        val username = getUsername() ?: return null
        val password = getPassword() ?: return null

        return StoredCredentials(serverUrl, username, password)
    }
}

data class StoredCredentials(
    val serverUrl: String,
    val username: String,
    val password: String
)