package com.example.nova.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Jellyseer request/media item
@Serializable
data class JellyseerMediaDto(
    val id: Int,
    val externalId: String? = null, // TMDB ID
    val title: String,
    val overview: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val releaseDate: String? = null,
    val voteAverage: Double? = null,
    val status: String? = null, // For Series: "upcoming", "returning", "ended"
    val mediaType: String, // "movie" or "tv"
    val requests: List<JellyseerRequestDto>? = emptyList(),
    val available: Boolean = false,
    val inProgress: Boolean = false,
    val genreIds: List<Int>? = emptyList()
)

@Serializable
data class JellyseerRequestDto(
    val id: Int,
    val status: String, // "pending", "approved", "declined", "completed"
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val requestedBy: RequestUserDto? = null,
    val approvedBy: RequestUserDto? = null
)

@Serializable
data class RequestUserDto(
    val id: String,
    val username: String,
    val email: String? = null
)

// Search/discover response
@Serializable
data class JellyseerMediaResponse(
    val results: List<JellyseerMediaDto> = emptyList(),
    val page: Int = 1,
    val totalPages: Int = 0,
    val totalResults: Int = 0
)

// Request creation
@Serializable
data class JellyseerRequestInput(
    val mediaId: Int,
    val mediaType: String, // "movie" or "tv"
    val seasons: List<Int>? = null // For TV shows, specific season numbers
)

// Settings/configuration
@Serializable
data class JellyseerSettingsDto(
    val publicRequests: Boolean = false,
    val movieRequestsEnabled: Boolean = true,
    val tvRequestsEnabled: Boolean = true
)