package com.example.madagascar.Mypage

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.API.FavoriteItem
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoritesAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2열 그리드 레이아웃 설정

        // Firebase 설정
        firestore = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 즐겨찾기 데이터 로드
        loadFavorites()

        // 뒤로가기 버튼 설정
        val arrowBtn102 = findViewById<ImageView>(R.id.btn_arrow102)
        arrowBtn102.setOnClickListener {
            finish() // 현재 Activity 종료
        }
    }

    private fun loadFavorites() {
        firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .get()
            .addOnSuccessListener { documents ->
                val favorites = documents.map { doc ->
                    FavoriteItem(
                        doc.id, // contentId
                        doc.getString("title") ?: "제목 없음",
                        doc.getString("image")
                    )
                }
                adapter = FavoritesAdapter(this, favorites.toMutableList())
                recyclerView.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "즐겨찾기 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
    }
}