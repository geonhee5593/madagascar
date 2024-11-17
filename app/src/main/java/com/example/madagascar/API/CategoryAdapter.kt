package com.example.madagascar.API

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.R

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category?) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var selectedCategory: Int? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.categoryIcon)
        val name: TextView = view.findViewById(R.id.categoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val category = categories[position]
        holder.name.text = category.name
        holder.icon.setImageResource(category.icon)

        // 선택된 카테고리에 따라 배경색 변경
        if (position == selectedCategory) {
            holder.itemView.setBackgroundResource(R.drawable.selected_background)
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent)
        }

        holder.itemView.setOnClickListener {
            if (selectedCategory == position) {
                // 동일한 카테고리를 다시 클릭하면 선택 해제 및 전체 축제 조회
                selectedCategory = null
                onCategoryClick(null) // 전체 축제 조회
            } else {
                // 새로운 카테고리 선택
                selectedCategory = position
                onCategoryClick(category)
            }
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = categories.size
}