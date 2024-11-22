package com.example.madagascar.API

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.R
import android.view.View

class MonthListAdapter(
    private val months: List<String>,
    private val festivalsByMonth: Map<String, List<FestivalItem>>,
    private val onFestivalClick: (FestivalItem) -> Unit
) : RecyclerView.Adapter<MonthListAdapter.MonthViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.month_festival_item, parent, false)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val month = months[position]
        val festivals = festivalsByMonth[month] ?: emptyList()
        holder.bind(month, festivals)
    }

    override fun getItemCount(): Int = months.size

    inner class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val monthTitle: TextView = itemView.findViewById(R.id.monthTitle)
        private val festivalRecyclerView: RecyclerView = itemView.findViewById(R.id.festivalRecyclerView)

        fun bind(month: String, festivals: List<FestivalItem>) {
            monthTitle.text = month
            festivalRecyclerView.layoutManager =
                LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)

            festivalRecyclerView.adapter = FestivalAdapter(festivals.toMutableList()) { festival ->
                onFestivalClick(festival)
            }
        }
    }
}