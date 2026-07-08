package com.example.nova.data

/**
 * Unified representation of content across Jellyfin and Jellyseer
 * UI works only with this, doesn't care about the source
 */
data class UnifiedItem(
    val id: String, // Unique ID within this source
    val sourceId: String, // "jellyfin" or "jellyseer"
    val title: String,
    val description: String? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val contentType: String, // "movie", "series", "episode", "collection"
    val rating: Double? = null,
    val releaseDate: String? = null,
    val runtime: Int? = null, // in minutes
    val genres: List<String> = emptyList(),
    val overview: String? = null,

    // State indicators
    val isAvailable: Boolean = true, // True if in Jellyfin library
    val isRequested: Boolean = false, // True if user requested via Jellyseer
    val requestStatus: String? = null, // "pending", "approved", "declined" from Jellyseer
    val progress: Float = 0f, // 0-1 for watch progress on Jellyfin items

    // Extra metadata
    val externalId: String? = null, // TMDB ID, etc.
    val year: Int? = null,
    val userRating: Double? = null
)

// Badge to show on the UI
enum class ContentBadge {
    JELLYFIN,      // Already have it
    JELLYSEER,     // Requested it
    COMING_SOON    // Upcoming release / pending approval
}

fun UnifiedItem.getBadges(): List<ContentBadge> {
    val badges = mutableListOf<ContentBadge>()

    when (sourceId) {
        "jellyfin" -> {
            if (isAvailable) badges.add(ContentBadge.JELLYFIN)
        }
        "jellyseer" -> {
            badges.add(ContentBadge.JELLYSEER)
            // If it's approved and available, also show Jellyfin badge
            if (requestStatus == "approved" && isAvailable) {
                badges.add(ContentBadge.JELLYFIN)
            }
            // Show coming soon if pending or declined
            if (requestStatus == "pending" || requestStatus == "declined") {
                badges.add(ContentBadge.COMING_SOON)
            }
        }
    }

    return badges
}

// Extension to convert from different sources
fun ItemDto.toUnifiedItem(): UnifiedItem = UnifiedItem(
    id = this.Id,
    sourceId = "jellyfin",
    title = this.Name,
    description = this.Overview,
    posterUrl = null, // Will be filled by repository with image URL
    backdropUrl = null,
    contentType = when (this.Type) {
        "Movie" -> "movie"
        "Series" -> "series"
        "Episode" -> "episode"
        "Season" -> "season"
        else -> "collection"
    },
    rating = this.CommunityRating,
    releaseDate = null,
    runtime = this.RunTimeTicks?.let { (it / 600000000).toInt() }, // Convert from ticks to minutes
    genres = this.GenreItems?.map { it.Name } ?: emptyList(),
    overview = this.Overview,
    isAvailable = true,
    progress = (this.UserData?.PlaybackPositionTicks?.toFloat() ?: 0f) / (this.RunTimeTicks?.toFloat() ?: 1f),
    year = this.ProductionYear
)

fun JellyseerMediaDto.toUnifiedItem(): UnifiedItem = UnifiedItem(
    id = this.id.toString(),
    sourceId = "jellyseer",
    title = this.title,
    description = this.overview,
    posterUrl = this.posterPath,
    backdropUrl = this.backdropPath,
    contentType = this.mediaType,
    rating = this.voteAverage,
    releaseDate = this.releaseDate,
    genres = emptyList(),
    overview = this.overview,
    isAvailable = this.available,
    isRequested = !this.requests.isNullOrEmpty(),
    requestStatus = this.requests?.firstOrNull()?.status,
    externalId = this.externalId,
    year = this.releaseDate?.take(4)?.toIntOrNull()
)