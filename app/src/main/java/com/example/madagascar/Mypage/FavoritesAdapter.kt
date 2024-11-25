package com.example.madagascar.Mypage

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.madagascar.API.DetailActivity
import com.example.madagascar.API.FavoriteItem
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesAdapter(
    private val context: Context,
    private val favorites: MutableList<FavoriteItem>,
    private val onItemClick: (FavoriteItem) -> Unit // 클릭 리스너 추가
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.favoriteTitle)
        val image = itemView.findViewById<ImageView>(R.id.favoriteImage)
        val favoriteButton = itemView.findViewById<ImageView>(R.id.favoriteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val favorite = favorites[position]
        Log.d("FavoritesAdapter", "즐겨찾기 항목 바인딩: ${favorite.title}, ${favorite.firstimage}") // 바인딩 로그

        holder.title.text = favorite.title
        Glide.with(context).load(favorite.firstimage).into(holder.image)

        // 이미지 클릭 시 디테일 창으로 이동
        holder.image.setOnClickListener {
            Log.d("FavoritesAdapter", "이미지 클릭됨: ${favorite.title}")

            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra("contentId", favorite.contentId) // contentId 전달
            }
            context.startActivity(intent)
        }

        // 즐겨찾기 삭제 처리
        holder.favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
        holder.favoriteButton.setOnClickListener {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("favorites")
                .document(favorite.contentId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "즐겨찾기에서 제거되었습니다.", Toast.LENGTH_SHORT).show()
                    favorites.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, favorites.size)
                }
                .addOnFailureListener {
                    Toast.makeText(context, "즐겨찾기 삭제 실패.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount() = favorites.size
}