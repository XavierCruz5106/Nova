package com.example.nova.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient

class JellyfinRepository(private val context: Context) {
    private val authManager = AuthManager(context)

    private var apiService: JellyfinService? = null
    private var currentServerUrl: String? = null

    // Initialize API service with server URL
    private suspend fun ensureApiServiceInitialized(serverUrl: String) {
        if (apiService == null || currentServerUrl != serverUrl) {
            currentServerUrl = serverUrl
            apiService = buildRetrofitService(serverUrl)
        }
    }

    // Build Retrofit service with auth interceptor
    private fun buildRetrofitService(baseUrl: String): JellyfinService {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authManager))
            .build()

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl.trimEnd('/') + "/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        return retrofit.create(JellyfinService::class.java)
    }

    // Login with username and password
    suspend fun login(
        serverUrl: String,
        username: String,
        password: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            ensureApiServiceInitialized(serverUrl)

            val request = AuthenticationRequest(
                Username = username,
                Password = password,
                RememberMe = true
            )

            val response = apiService!!.authenticate(request)

            // Save credentials for future use
            authManager.saveCredentials(
                serverUrl = serverUrl,
                username = username,
                password = password,
                accessToken = response.AccessToken,
                userId = response.User.Id,
                serverId = response.ServerId
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Attempt auto-login with stored credentials
    suspend fun autoLogin(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val stored = authManager.getStoredCredentials() ?: return@withContext Result.failure(
                Exception("No stored credentials")
            )

            ensureApiServiceInitialized(stored.serverUrl)

            val request = AuthenticationRequest(
                Username = stored.username,
                Password = stored.password,
                RememberMe = true
            )

            val response = apiService!!.authenticate(request)

            // Update token
            authManager.saveCredentials(
                serverUrl = stored.serverUrl,
                username = stored.username,
                password = stored.password,
                accessToken = response.AccessToken,
                userId = response.User.Id,
                serverId = response.ServerId
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Logout and clear credentials
    fun logout() {
        authManager.clearCredentials()
        apiService = null
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean = authManager.isLoggedIn()

    // Get user ID
    fun getCurrentUserId(): String? = authManager.getUserId()

    // Get libraries
    suspend fun getLibraries(): Result<List<LibraryDto>> = withContext(Dispatchers.IO) {
        try {
            val userId = authManager.getUserId() ?: throw Exception("Not logged in")
            ensureApiServiceInitialized(authManager.getServerUrl()!!)

            val response = apiService!!.getLibraries(userId)
            Result.success(response.Items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get items from a library
    suspend fun getItemsByParent(
        parentId: String,
        includeItemTypes: String? = null,
        limit: Int = 50
    ): Result<List<ItemDto>> = withContext(Dispatchers.IO) {
        try {
            val userId = authManager.getUserId() ?: throw Exception("Not logged in")
            ensureApiServiceInitialized(authManager.getServerUrl()!!)

            val response = apiService!!.getItems(
                userId = userId,
                parentId = parentId,
                limit = limit,
                includeItemTypes = includeItemTypes
            )
            Result.success(response.Items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get latest items (for home screen)
    suspend fun getLatestItems(limit: Int = 16): Result<List<ItemDto>> = withContext(Dispatchers.IO) {
        try {
            val userId = authManager.getUserId() ?: throw Exception("Not logged in")
            ensureApiServiceInitialized(authManager.getServerUrl()!!)

            val items = apiService!!.getLatestItems(
                userId = userId,
                limit = limit
            )
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Search items
    suspend fun search(query: String, limit: Int = 30): Result<List<ItemDto>> = withContext(Dispatchers.IO) {
        try {
            val userId = authManager.getUserId() ?: throw Exception("Not logged in")
            ensureApiServiceInitialized(authManager.getServerUrl()!!)

            val response = apiService!!.search(
                userId = userId,
                searchTerm = query,
                limit = limit
            )
            Result.success(response.Items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get playback info for an item
    suspend fun getPlaybackInfo(itemId: String): Result<PlaybackInfoResponse> = withContext(Dispatchers.IO) {
        try {
            val userId = authManager.getUserId() ?: throw Exception("Not logged in")
            ensureApiServiceInitialized(authManager.getServerUrl()!!)

            val request = PlaybackInfoRequest(
                UserId = userId,
                ItemId = itemId
            )

            val response = apiService!!.getPlaybackInfo(itemId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Build image URL for an item
    fun getImageUrl(
        itemId: String,
        imageType: String = "Primary",
        tag: String? = null,
        width: Int? = null
    ): String? {
        val serverUrl = authManager.getServerUrl() ?: return null
        val token = authManager.getAccessToken() ?: return null

        val params = mutableListOf<String>()
        if (tag != null) params.add("tag=$tag")
        if (width != null) params.add("maxWidth=$width")
        params.add("quality=90")

        val queryString = if (params.isNotEmpty()) "?${params.joinToString("&")}" else ""

        return "$serverUrl/Items/$itemId/Images/$imageType$queryString&api_key=$token"
    }
}

// Interceptor to add auth token to requests
private class AuthInterceptor(private val authManager: AuthManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val original = chain.request()

        val requestBuilder = original.newBuilder()

        // Add auth token if available
        authManager.getAccessToken()?.let {
            requestBuilder.header("Authorization", "MediaBrowser Token=\"$it\"")
        }

        // Add required Jellyfin headers
        requestBuilder.header("X-MediaBrowser-Token", authManager.getAccessToken() ?: "")

        return chain.proceed(requestBuilder.build())
    }
}