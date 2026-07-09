package com.example.nova.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nova.R
import com.example.nova.data.UnifiedItem

class MediaItemAdapter(
    private val items: List<UnifiedItem>,
    private val onItemClick: (UnifiedItem) -> Unit,
    private val onRequestClick: (UnifiedItem) -> Unit
) : RecyclerView.Adapter<MediaItemAdapter.MediaItemViewHolder>() {

    inner class MediaItemViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val posterImage: ImageView = itemView.findViewById(R.id.poster_image)
        private val titleText: TextView = itemView.findViewById(R.id.title_text)
        private val yearText: TextView = itemView.findViewById(R.id.year_text)
        private val ratingText: TextView = itemView.findViewById(R.id.rating_text)
        private val badgeText: TextView = itemView.findViewById(R.id.badge_text)
        private val badgeContainer: android.view.View = itemView.findViewById(R.id.badge_container)

        fun bind(item: UnifiedItem) {
            titleText.text = item.title
            yearText.text = item.year?.toString() ?: "—"
            ratingText.text = if (item.rating != null) "★ ${String.format("%.1f", item.rating)}" else "★ —"

            // Set badge
            when {
                item.isAvailable && item.sourceId == "jellyfin" -> {
                    badgeText.text = "In Library"
                    badgeContainer.setBackgroundColor(itemView.context.getColor(android.R.color.holo_green_dark))
                }
                item.requestStatus == "requested" -> {
                    badgeText.text = "Requested"
                    badgeContainer.setBackgroundColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                }
                else -> {
                    badgeText.text = "Request"
                    badgeContainer.setBackgroundColor(itemView.context.getColor(android.R.color.holo_blue_dark))
                }
            }

            // Load poster image (placeholder for now)
            posterImage.setBackgroundColor(
                android.graphics.Color.HSVToColor(
                    floatArrayOf((item.id.hashCode() % 360).toFloat(), 0.7f, 0.8f)
                )
            )

            itemView.setOnClickListener { onItemClick(item) }
            badgeContainer.setOnClickListener { onRequestClick(item) }

            // Handle focus for Fire TV D-pad navigation
            itemView.setOnFocusChangeListener { v, hasFocus ->
                val focusBorder = itemView.findViewById<View>(R.id.focus_border)
                focusBorder.visibility = if (hasFocus) View.VISIBLE else View.GONE
            }

            // Set focusable for D-pad navigation
            itemView.isFocusable = true
            itemView.isClickable = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media_card, parent, false)
        return MediaItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MediaItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}