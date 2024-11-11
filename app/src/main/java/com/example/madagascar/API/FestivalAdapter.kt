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

class FestivalAdapter : RecyclerView.Adapter<FestivalAdapter.ViewHolder>() {

    private val festivals = mutableListOf<FestivalItem>()

    fun setFestivals(festivalList: List<FestivalItem>) {
        festivals.clear()
        festivals.addAll(festivalList)
        notifyDataSetChanged()
    }

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

        val imageUrl = festival.firstimage
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .apply(RequestOptions()
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.image)
        } else {
            holder.image.setImageResource(android.R.drawable.stat_notify_error)
        }
    }

    override fun getItemCount(): Int = festivals.size
}