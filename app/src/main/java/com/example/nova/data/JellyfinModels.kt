package com.example.nova.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Authentication
@Serializable
data class AuthenticationRequest(
    val Username: String,
    val Password: String,
    val RememberMe: Boolean = true
)

@Serializable
data class AuthenticationResponse(
    val User: UserDto,
    val AccessToken: String,
    val ServerId: String
)

// User
@Serializable
data class UserDto(
    val Id: String,
    val Name: String,
    val PrimaryImageTag: String? = null,
    val HasPassword: Boolean = false
)

// Library/Collection
@Serializable
data class LibraryDto(
    val Name: String,
    val Id: String,
    val CollectionType: String? = null,
    val PrimaryImageTag: String? = null,
    val ImageTags: Map<String, String>? = null
)

@Serializable
data class LibrariesResponse(
    val Items: List<LibraryDto> = emptyList(),
    val TotalRecordCount: Int = 0
)

// Media Item (Movie, Show, Episode, etc.)
@Serializable
data class ItemDto(
    val Id: String,
    val Name: String,
    val Type: String, // Movie, Series, Episode, Season, etc.
    val ParentId: String? = null,
    val SeriesId: String? = null,
    val Overview: String? = null,
    val PrimaryImageTag: String? = null,
    val BackdropImageTags: List<String>? = emptyList(),
    val ImageTags: Map<String, String>? = null,
    val RunTimeTicks: Long? = null, // 100ns ticks
    val CommunityRating: Double? = null,
    val OfficialRating: String? = null,
    val ProductionYear: Int? = null,
    val GenreItems: List<GenreDto>? = emptyList(),
    val UserData: UserDataDto? = null,
    val IsFolder: Boolean = false,
    val ChildCount: Int? = null
)

@Serializable
data class GenreDto(
    val Name: String,
    val Id: String
)

@Serializable
data class UserDataDto(
    val PlaybackPositionTicks: Long = 0,
    val PlayCount: Int = 0,
    val IsFavorite: Boolean = false,
    val UnplayedItemCount: Int? = null
)

@Serializable
data class ItemsResponse(
    val Items: List<ItemDto> = emptyList(),
    val TotalRecordCount: Int = 0
)

// Image URLs helper
data class ImageUrl(
    val type: String,
    val tag: String
)

// Playback info
@Serializable
data class PlaybackInfoRequest(
    val UserId: String,
    val ItemId: String,
    val IsPlayback: Boolean = true,
    val AutoOpenLiveStream: Boolean = false,
    val AllowVideoStreamCopy: Boolean = true,
    val AllowAudioStreamCopy: Boolean = true
)

@Serializable
data class PlaybackInfoResponse(
    val Id: String,
    val MediaSources: List<MediaSourceDto>? = emptyList(),
    val PlaySessionId: String
)

@Serializable
data class MediaSourceDto(
    val Id: String,
    val Path: String? = null,
    val Protocol: String,
    val TranscodingUrl: String? = null,
    val DirectStreamUrl: String? = null,
    val Bitrate: Long? = null,
    val Container: String? = null
)