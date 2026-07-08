package com.example.nova

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nova.data.MediaRepository
import com.example.nova.data.ConfigManager
import com.example.nova.data.JellyfinRepository
import com.example.nova.ui.HomeViewModel
import com.example.nova.ui.SectionAdapter
import com.example.nova.ui.HomeUiState
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchView: SearchView
    private var sectionAdapter: SectionAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        homeViewModel = ViewModelProvider(
            this,
            HomeViewModelFactory(requireContext())
        ).get(HomeViewModel::class.java)

        recyclerView = view.findViewById(R.id.sections_recycler)
        progressBar = view.findViewById(R.id.loading_progress)
        searchView = view.findViewById(R.id.search_view)

        // Set up RecyclerView
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        // Set up search
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    homeViewModel.search(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    homeViewModel.clearSearch()
                }
                return true
            }
        })

        // Observe home state
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.homeState.collect { state ->
                when (state) {
                    is HomeUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                    is HomeUiState.Success -> {
                        progressBar.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        displaySections(state.content.recentlyWatched, state.content.trending)
                    }
                    is HomeUiState.Error -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Error loading content: ${state.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun displaySections(recentlyWatched: List<com.example.nova.data.UnifiedItem>, trending: List<com.example.nova.data.UnifiedItem>) {
        val sections = listOf(
            com.example.nova.data.HomeScreenSection(
                id = "continue_watching",
                title = "Continue watching",
                sectionType = com.example.nova.data.SectionType.CONTINUE_WATCHING,
                items = recentlyWatched
            ),
            com.example.nova.data.HomeScreenSection(
                id = "trending",
                title = "Trending now",
                sectionType = com.example.nova.data.SectionType.TRENDING,
                items = trending
            )
        )

        sectionAdapter = SectionAdapter(
            sections,
            onItemClick = { item ->
                // TODO: Navigate to detail screen
            },
            onRequestClick = { item ->
                if (item.sourceId == "jellyseer") {
                    homeViewModel.requestContent(item.id.toInt(), item.contentType)
                }
            },
            onSectionExpand = { sectionId ->
                // TODO: Expand section inline
            }
        )

        recyclerView.adapter = sectionAdapter
    }
}

// ViewModel Factory
class HomeViewModelFactory(private val context: android.content.Context) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        val jellyfinRepository = JellyfinRepository(context)
        val configManager = ConfigManager(context)
        val mediaRepository = MediaRepository(jellyfinRepository, configManager)
        return HomeViewModel(mediaRepository) as T
    }
}