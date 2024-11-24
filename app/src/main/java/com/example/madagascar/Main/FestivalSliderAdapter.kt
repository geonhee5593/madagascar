package com.example.madagascar.Main

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.madagascar.API.DetailActivity
import com.example.madagascar.API.FestivalItem
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth

class FestivalSliderAdapter(
    private val context: Context,
    private val festivalList: List<FestivalItem>
) : RecyclerView.Adapter<FestivalSliderAdapter.FestivalViewHolder>() {

    inner class FestivalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val festivalImage: ImageView = itemView.findViewById(R.id.festivalImage)
        val festivalInfo: TextView = itemView.findViewById(R.id.festivalInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FestivalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_main, parent, false)
        return FestivalViewHolder(view)
    }

    override fun onBindViewHolder(holder: FestivalViewHolder, position: Int) {
        val festival = festivalList[position]

        // 축제 정보 설정
        holder.festivalInfo.text =
            "${festival.title}\n기간: ${festival.eventStartDate} ~ ${festival.eventEndDate}\n주소: ${festival.addr1}"

        // 축제 이미지 설정
        if (!festival.firstImage.isNullOrEmpty()) {
            Glide.with(context).load(festival.firstImage).into(holder.festivalImage)
        } else {
            holder.festivalImage.setImageResource(R.drawable.default_image) // 기본 이미지
        }

        // 클릭 이벤트: DetailActivity로 이동
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("userId", FirebaseAuth.getInstance().currentUser?.uid)
            intent.putExtra("contentId", festival.contentId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = festivalList.size
}