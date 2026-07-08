package com.example.nova.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nova.R
import com.example.nova.data.HomeScreenSection
import com.example.nova.data.UnifiedItem

class SectionAdapter(
    private val sections: List<HomeScreenSection>,
    private val onItemClick: (UnifiedItem) -> Unit,
    private val onRequestClick: (UnifiedItem) -> Unit,
    private val onSectionExpand: (String) -> Unit
) : RecyclerView.Adapter<SectionAdapter.SectionViewHolder>() {

    inner class SectionViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val sectionTitle: TextView = itemView.findViewById(R.id.section_title)
        private val viewMoreButton: Button = itemView.findViewById(R.id.view_more_button)
        private val horizontalRecycler: RecyclerView = itemView.findViewById(R.id.horizontal_recycler)
        private val sectionContainer: LinearLayout = itemView.findViewById(R.id.section_container)

        fun bind(section: HomeScreenSection) {
            sectionTitle.text = section.title

            // Set up horizontal RecyclerView for items
            val itemAdapter = MediaItemAdapter(
                if (section.isExpanded) section.items else section.items.take(8),
                onItemClick,
                onRequestClick
            )

            horizontalRecycler.apply {
                layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                adapter = itemAdapter
            }

            // Toggle view more/less button
            if (section.totalCount > 8) {
                viewMoreButton.visibility = android.view.View.VISIBLE
                viewMoreButton.text = if (section.isExpanded) "View less ↑" else "View more →"
                viewMoreButton.setOnClickListener {
                    onSectionExpand(section.id)
                }
            } else {
                viewMoreButton.visibility = android.view.View.GONE
            }

            // If expanded, show grid instead of horizontal scroll
            if (section.isExpanded) {
                horizontalRecycler.layoutManager = androidx.recyclerview.widget.GridLayoutManager(
                    itemView.context,
                    4
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_section, parent, false)
        return SectionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        holder.bind(sections[position])
    }

    override fun getItemCount() = sections.size
}