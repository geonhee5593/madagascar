package com.example.madagascar.API

import android.util.Log
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class FestivalAdapter(
    private var festivals:  MutableList<FestivalItem>,
    private val onItemClick: (FestivalItem) -> Unit
) : RecyclerView.Adapter<FestivalAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val address: TextView = view.findViewById(R.id.address)
        val image: ImageView = view.findViewById(R.id.image)
        val eventDate: TextView = view.findViewById(R.id.event_date)
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

        val startDateFormatted = formatDate(festival.eventStartDate)
        val endDateFormatted = formatDate(festival.eventEndDate)
        holder.eventDate.text = "기간: $startDateFormatted ~ $endDateFormatted"


        // 클릭 이벤트
        holder.itemView.setOnClickListener {
            onItemClick(festival)
        }
    }

    override fun getItemCount(): Int = festivals.size

    fun addFestivals(newFestivals: List<FestivalItem>) {
        val existingIds = festivals.map { it.contentId }.toSet()
        val uniqueFestivals = newFestivals.filter { it.contentId !in existingIds }
            notifyDataSetChanged()
        }

    fun setFestivals(newFestivals: List<FestivalItem>) {
        festivals.clear()
        festivals.addAll(newFestivals)
        notifyDataSetChanged()
    }

    // 날짜 포맷 변환 함수
    private fun formatDate(date: String?): String {
        return try {
            if (date.isNullOrEmpty() || date.length != 8) return "날짜 없음"
            val originalFormat = DateTimeFormatter.ofPattern("yyyyMMdd")
            val targetFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd")
            val parsedDate = LocalDate.parse(date, originalFormat)
            targetFormat.format(parsedDate)
        } catch (e: Exception) {
            "날짜 없음"
        }
    }
    /**
     * 특정 축제의 기간 정보 업데이트
     */
    fun updateFestivalDates(contentId: String, startDate: String?, endDate: String?) {
        festivals.find { it.contentId == contentId }?.let { festival ->
            festival.eventStartDate = startDate ?: "기간 정보 없음"
            festival.eventEndDate = endDate ?: "기간 정보 없음"
            notifyDataSetChanged()
        }
    }

    fun getFestivals(): List<FestivalItem> {
        return festivals
    }
}