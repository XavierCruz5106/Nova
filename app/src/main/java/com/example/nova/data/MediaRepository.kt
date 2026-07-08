package com.example.nova.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient

/**
 * Unified media repository that merges Jellyfin and Jellyseer content
 */
class MediaRepository(
    private val jellyfinRepository: JellyfinRepository,
    private val configManager: ConfigManager
) {
    private var jellyseerService: JellyseerService? = null

    // Build Jellyseer service lazily
    private suspend fun getJellyseerService(): JellyseerService? = withContext(Dispatchers.IO) {
        if (jellyseerService == null) {
            val url = configManager.getJellyseerUrl()
            val apiKey = configManager.getJellyseerApiKey()

            if (!url.isNullOrBlank() && apiKey.isNotBlank()) {
                jellyseerService = buildJellyseerService(url, apiKey)
            }
        }
        jellyseerService
    }

    private fun buildJellyseerService(baseUrl: String, apiKey: String): JellyseerService {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(JellyseerAuthInterceptor(apiKey))
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

        return retrofit.create(JellyseerService::class.java)
    }

    /**
     * Get home screen content:
     * - Jellyfin latest/recent
     * - Jellyseer trending/approved
     */
    suspend fun getHomeContent(): Result<HomeContent> = withContext(Dispatchers.IO) {
        try {
            val jellyfinLatest = async { jellyfinRepository.getLatestItems(limit = 16) }
            val jellyseerTrending = async {
                getJellyseerService()?.let { service ->
                    try {
                        service.getTrendingMedia(page = 1)
                    } catch (e: Exception) {
                        JellyseerMediaResponse()
                    }
                } ?: JellyseerMediaResponse()
            }

            val jellyfinResult = jellyfinLatest.await()
            val jellyseerResult = jellyseerTrending.await()

            val jellyfinItems = jellyfinResult.getOrNull()?.map { it.toUnifiedItem() } ?: emptyList()
            val jellyseerItems = jellyseerResult.results.map { it.toUnifiedItem() }

            val homeContent = HomeContent(
                recentlyWatched = jellyfinItems.take(8),
                trending = jellyseerItems.take(8)
            )

            Result.success(homeContent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search across both Jellyfin and Jellyseer
     */
    suspend fun search(query: String): Result<List<UnifiedItem>> = withContext(Dispatchers.IO) {
        try {
            val jellyfinSearch = async { jellyfinRepository.search(query, limit = 15) }
            val jellyseerSearch = async {
                getJellyseerService()?.let { service ->
                    try {
                        val movies = service.searchMovies(query, page = 1)
                        val tv = service.searchTv(query, page = 1)
                        (movies.results + tv.results)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } ?: emptyList()
            }

            val jellyfinResults = jellyfinSearch.await().getOrNull() ?: emptyList()
            val jellyseerResults = jellyseerSearch.await()

            val jellyfinItems = jellyfinResults.map { it.toUnifiedItem() }
            val jellyseerItems = jellyseerResults.map { it.toUnifiedItem() }

            // Combine and deduplicate by title
            val combined = (jellyfinItems + jellyseerItems).distinctBy { it.title.lowercase() }

            Result.success(combined)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Browse a library by parent ID (Jellyfin only)
     */
    suspend fun browseLibrary(
        parentId: String,
        includeItemTypes: String? = null
    ): Result<List<UnifiedItem>> = withContext(Dispatchers.IO) {
        try {
            val result = jellyfinRepository.getItemsByParent(parentId, includeItemTypes, limit = 50)
            val items = result.getOrNull()?.map { it.toUnifiedItem() } ?: emptyList()
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get Jellyseer requests (what user has requested)
     */
    suspend fun getRequests(): Result<List<UnifiedItem>> = withContext(Dispatchers.IO) {
        try {
            val service = getJellyseerService() ?: return@withContext Result.success(emptyList())
            val response = service.getUserRequests()
            val items = response.results.map { it.toUnifiedItem() }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Make a request on Jellyseer
     */
    suspend fun requestContent(
        mediaId: Int,
        mediaType: String, // "movie" or "tv"
        seasons: List<Int>? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val service = getJellyseerService() ?: throw Exception("Jellyseer not configured")
            val request = JellyseerRequestInput(
                mediaId = mediaId,
                mediaType = mediaType,
                seasons = seasons
            )
            service.createRequest(request)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get item details with playback info (Jellyfin)
     */
    suspend fun getItemDetails(itemId: String): Result<UnifiedItem> = withContext(Dispatchers.IO) {
        try {
            val result = jellyfinRepository.getPlaybackInfo(itemId)
            // In a real app, you'd fetch the full ItemDto and convert it
            // For now, return success with playback info available
            Result.success(UnifiedItem(
                id = itemId,
                sourceId = "jellyfin",
                title = "Item",
                contentType = "movie"
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get image URL for an item
     */
    fun getImageUrl(item: UnifiedItem): String? {
        return when (item.sourceId) {
            "jellyfin" -> jellyfinRepository.getImageUrl(item.id, tag = item.externalId)
            "jellyseer" -> {
                // Jellyseer returns full URLs in posterPath/backdropPath
                item.posterUrl ?: item.backdropUrl
            }
            else -> null
        }
    }
}

/**
 * Home screen content
 */
data class HomeContent(
    val recentlyWatched: List<UnifiedItem>,
    val trending: List<UnifiedItem>
)

/**
 * Interceptor for Jellyseer API key
 */
private class JellyseerAuthInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val original = chain.request()

        val requestBuilder = original.newBuilder()
            .header("X-API-Key", apiKey)

        return chain.proceed(requestBuilder.build())
    }
}