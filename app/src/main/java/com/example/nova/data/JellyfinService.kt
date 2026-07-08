package com.example.nova.data

import retrofit2.http.*

interface JellyfinService {

    // Authentication
    @POST("/Users/AuthenticateByName")
    suspend fun authenticate(
        @Body request: AuthenticationRequest
    ): AuthenticationResponse

    // Libraries
    @GET("/Library/MediaFolders")
    suspend fun getLibraries(
        @Query("userId") userId: String
    ): LibrariesResponse

    // Items (generic media query)
    @GET("/Items")
    suspend fun getItems(
        @Query("userId") userId: String,
        @Query("parentId") parentId: String? = null,
        @Query("sortBy") sortBy: String = "SortName",
        @Query("sortOrder") sortOrder: String = "Ascending",
        @Query("startIndex") startIndex: Int = 0,
        @Query("limit") limit: Int = 50,
        @Query("imageTypeLimit") imageTypeLimit: Int = 1,
        @Query("enableImageTypes") enableImageTypes: String = "Primary,Backdrop,Banner",
        @Query("recursive") recursive: Boolean = true,
        @Query("includeItemTypes") includeItemTypes: String? = null,
        @Query("excludeItemTypes") excludeItemTypes: String? = null
    ): ItemsResponse

    // Get single item
    @GET("/Items/{itemId}")
    suspend fun getItem(
        @Path("itemId") itemId: String,
        @Query("userId") userId: String
    ): ItemDto

    // Latest items (for home screen)
    @GET("/Users/{userId}/Items/Latest")
    suspend fun getLatestItems(
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 16,
        @Query("includeItemTypes") includeItemTypes: String? = null,
        @Query("imageTypeLimit") imageTypeLimit: Int = 1,
        @Query("enableImageTypes") enableImageTypes: String = "Primary,Backdrop,Banner"
    ): List<ItemDto>

    // Recommended items
    @GET("/Users/{userId}/Items/Latest")
    suspend fun getRecommendedItems(
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 16,
        @Query("isPlayed") isPlayed: Boolean = false,
        @Query("imageTypeLimit") imageTypeLimit: Int = 1,
        @Query("enableImageTypes") enableImageTypes: String = "Primary,Backdrop,Banner"
    ): List<ItemDto>

    // Search
    @GET("/Items")
    suspend fun search(
        @Query("userId") userId: String,
        @Query("searchTerm") searchTerm: String,
        @Query("limit") limit: Int = 30,
        @Query("imageTypeLimit") imageTypeLimit: Int = 1,
        @Query("enableImageTypes") enableImageTypes: String = "Primary,Backdrop,Banner"
    ): ItemsResponse

    // Playback info
    @POST("/Items/{itemId}/PlaybackInfo")
    suspend fun getPlaybackInfo(
        @Path("itemId") itemId: String,
        @Body request: PlaybackInfoRequest
    ): PlaybackInfoResponse

    // Mark item as played/unplayed
    @POST("/Users/{userId}/PlayedItems/{itemId}")
    suspend fun markAsPlayed(
        @Path("userId") userId: String,
        @Path("itemId") itemId: String
    )

    @DELETE("/Users/{userId}/PlayedItems/{itemId}")
    suspend fun markAsUnplayed(
        @Path("userId") userId: String,
        @Path("itemId") itemId: String
    )

    // Favorites
    @POST("/Users/{userId}/FavoriteItems/{itemId}")
    suspend fun addFavorite(
        @Path("userId") userId: String,
        @Path("itemId") itemId: String
    )

    @DELETE("/Users/{userId}/FavoriteItems/{itemId}")
    suspend fun removeFavorite(
        @Path("userId") userId: String,
        @Path("itemId") itemId: String
    )

    // Progress (resume)
    @POST("/Users/{userId}/PlayingItems/{itemId}/Progress")
    suspend fun reportProgress(
        @Path("userId") userId: String,
        @Path("itemId") itemId: String,
        @Query("positionTicks") positionTicks: Long
    )
}