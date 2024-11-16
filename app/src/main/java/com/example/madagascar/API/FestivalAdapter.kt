package com.example.madagascar.API

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.madagascar.R

class FestivalAdapter(
    private var festivals: List<FestivalItem>,
    private val onItemClick: (FestivalItem) -> Unit
) : RecyclerView.Adapter<FestivalAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val address: TextView = view.findViewById(R.id.address)
        val image: ImageView = view.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_festival, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val festival = festivals[position]
        holder.title.text = festival.title
        holder.address.text = festival.addr1

        // 이미지 로드
        val imageUrl = festival.firstImage
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image)
        } else {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // 클릭 이벤트
        holder.itemView.setOnClickListener {
            onItemClick(festival)
        }
    }

    override fun getItemCount(): Int = festivals.size

    fun setFestivals(newFestivals: List<FestivalItem>) {
        festivals = newFestivals
        notifyDataSetChanged()
    }
}