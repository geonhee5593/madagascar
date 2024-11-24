package com.example.madagascar.API

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.R
import android.view.View

class RegionListAdapter(
    private val regions: List<String>,
    private val festivalsByRegion: Map<String, List<FestivalItem>>,
    private val onFestivalClick: (FestivalItem) -> Unit
) : RecyclerView.Adapter<RegionListAdapter.RegionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.region_festival_item, parent, false)
        return RegionViewHolder(view)
    }

    override fun onBindViewHolder(holder: RegionViewHolder, position: Int) {
        val region = regions[position]
        val festivals = festivalsByRegion[region] ?: emptyList()
        holder.bind(region, festivals)
    }

    override fun getItemCount(): Int = regions.size

    inner class RegionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val regionTitle: TextView = itemView.findViewById(R.id.regionTitle)
        private val festivalRecyclerView: RecyclerView = itemView.findViewById(R.id.festivalRecyclerView)

        fun bind(region: String, festivals: List<FestivalItem>) {
            regionTitle.text = region
            festivalRecyclerView.layoutManager =
                LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)

            festivalRecyclerView.adapter = FestivalAdapter(festivals.toMutableList()) { festival ->
                onFestivalClick(festival)
            }
        }
    }
}