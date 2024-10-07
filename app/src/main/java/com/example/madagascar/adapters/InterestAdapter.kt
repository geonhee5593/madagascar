package com.example.madagascar.adapters

import com.example.madagascar.models.Interest  // Interest 클래스 임포트
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView // RecyclerView 임포트
import android.view.LayoutInflater

class InterestAdapter(
    private val interests: List<Interest>,
    private val onItemCheck: (Interest, Boolean) -> Unit
) : RecyclerView.Adapter<InterestAdapter.InterestViewHolder>() {

    inner class InterestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkbox_interest) // item_interest.xml에 id 확인

        fun bind(interest: Interest) {
            checkBox.text = interest.name
            checkBox.isChecked = interest.isSelected

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onItemCheck(interest, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_interest, parent, false)
        return InterestViewHolder(view)
    }

    override fun onBindViewHolder(holder: InterestViewHolder, position: Int) {
        holder.bind(interests[position])
    }

    override fun getItemCount() = interests.size
}
