package com.example.nova.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nova.data.HomeContent
import com.example.nova.data.MediaRepository
import com.example.nova.data.UnifiedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val content: HomeContent) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val results: List<UnifiedItem>) : SearchState()
    data class Error(val message: String) : SearchState()
}

class HomeViewModel(private val mediaRepository: MediaRepository) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val _selectedItem = MutableStateFlow<UnifiedItem?>(null)
    val selectedItem: StateFlow<UnifiedItem?> = _selectedItem.asStateFlow()

    init {
        loadHomeContent()
    }

    fun loadHomeContent() {
        viewModelScope.launch {
            _homeState.value = HomeUiState.Loading
            val result = mediaRepository.getHomeContent()

            result.onSuccess { content ->
                _homeState.value = HomeUiState.Success(content)
            }.onFailure { error ->
                _homeState.value = HomeUiState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _searchState.value = SearchState.Idle
            return
        }

        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            val result = mediaRepository.search(query)

            result.onSuccess { items ->
                _searchState.value = SearchState.Success(items)
            }.onFailure { error ->
                _searchState.value = SearchState.Error(error.message ?: "Search failed")
            }
        }
    }

    fun clearSearch() {
        _searchState.value = SearchState.Idle
    }

    fun selectItem(item: UnifiedItem) {
        _selectedItem.value = item
    }

    fun clearSelection() {
        _selectedItem.value = null
    }

    fun requestContent(mediaId: Int, mediaType: String) {
        viewModelScope.launch {
            mediaRepository.requestContent(mediaId, mediaType).onSuccess {
                // Refresh home content after request
                loadHomeContent()
            }
        }
    }
}