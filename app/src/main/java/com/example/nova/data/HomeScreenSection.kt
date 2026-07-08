package com.example.nova.data

data class HomeScreenSection(
    val id: String,
    val title: String,
    val sectionType: SectionType,
    val items: List<UnifiedItem>,
    val totalCount: Int = items.size,
    val isExpanded: Boolean = false
)

enum class SectionType {
    CONTINUE_WATCHING,
    RECENTLY_ADDED,
    GENRE,
    SIMILAR_TO,
    YOUR_REQUESTS,
    TRENDING
}