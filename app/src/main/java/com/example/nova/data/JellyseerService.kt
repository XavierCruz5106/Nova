package com.example.nova.data

import retrofit2.http.*

interface JellyseerService {

    // Discover/Search
    @GET("/api/v1/discover/movies")
    suspend fun discoverMovies(
        @Query("page") page: Int = 1,
        @Query("sort") sort: String = "popularity.desc"
    ): JellyseerMediaResponse

    @GET("/api/v1/discover/tv")
    suspend fun discoverTv(
        @Query("page") page: Int = 1,
        @Query("sort") sort: String = "popularity.desc"
    ): JellyseerMediaResponse

    // Search
    @GET("/api/v1/search/movies")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): JellyseerMediaResponse

    @GET("/api/v1/search/tv")
    suspend fun searchTv(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): JellyseerMediaResponse

    // Get specific media
    @GET("/api/v1/media/movie/{mediaId}")
    suspend fun getMovie(
        @Path("mediaId") mediaId: Int
    ): JellyseerMediaDto

    @GET("/api/v1/media/tv/{mediaId}")
    suspend fun getTv(
        @Path("mediaId") mediaId: Int
    ): JellyseerMediaDto

    // Requests - get all approved/available
    @GET("/api/v1/user/requests")
    suspend fun getUserRequests(
        @Query("status") status: String? = null, // "approved", "pending", "declined"
        @Query("sort") sort: String = "added"
    ): JellyseerMediaResponse

    // Create request
    @POST("/api/v1/request")
    suspend fun createRequest(
        @Body request: JellyseerRequestInput
    ): JellyseerMediaDto

    // Get approved media (content that was requested and approved)
    @GET("/api/v1/discover/approved")
    suspend fun getApprovedMedia(
        @Query("page") page: Int = 1
    ): JellyseerMediaResponse

    // Trending
    @GET("/api/v1/discover/trending")
    suspend fun getTrendingMedia(
        @Query("page") page: Int = 1
    ): JellyseerMediaResponse

    // Status check
    @GET("/api/v1/status")
    suspend fun getStatus(): JellyseerSettingsDto
}