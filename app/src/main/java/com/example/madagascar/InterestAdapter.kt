package com.example.madagascar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class InterestAdapter(private val interests: List<String>) :
    RecyclerView.Adapter<InterestAdapter.InterestViewHolder>() {

    private val selectedInterests = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_interest, parent, false)
        return InterestViewHolder(view)
    }

    override fun onBindViewHolder(holder: InterestViewHolder, position: Int) {
        val interest = interests[position]
        holder.bind(interest)

        // 클릭된 항목은 색상이 진하게 설정
        holder.itemView.setBackgroundColor(
            if (selectedInterests.contains(interest)) {
                ContextCompat.getColor(holder.itemView.context, R.color.dark_gray)
            } else {
                ContextCompat.getColor(holder.itemView.context, R.color.light_gray)
            }
        )

        // 항목 클릭 시 색상 변경
        holder.itemView.setOnClickListener {
            if (selectedInterests.contains(interest)) {
                selectedInterests.remove(interest)
                holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.light_gray)
                )
            } else {
                selectedInterests.add(interest)
                holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.dark_gray)
                )
            }
        }
    }

    override fun getItemCount() = interests.size

    // 선택된 관심 분야 반환
    fun getSelectedInterests(): List<String> {
        return selectedInterests.toList()
    }

    class InterestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val interestTextView: TextView = itemView.findViewById(R.id.tv_interest)

        fun bind(interest: String) {
            interestTextView.text = interest
        }
    }
}
