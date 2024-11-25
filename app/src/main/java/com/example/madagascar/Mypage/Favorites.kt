package com.example.madagascar.Mypage

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.API.DetailActivity
import com.example.madagascar.API.FavoriteItem
import com.example.madagascar.Main.MainActivity
import com.example.madagascar.Mylocation.fragmentActivity
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Favorites : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoritesAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        Log.d("FavoritesActivity", "FavoritesActivity 시작됨") // 액티비티 시작 로그

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2열 그리드 레이아웃 설정

        // Firebase 설정
        firestore = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("FavoritesActivity", "userId가 null입니다.") // Null 확인 로그
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("FavoritesActivity", "userId: $userId") // 유저 ID 확인 로그

        // 즐겨찾기 데이터 로드
        loadFavorites()

        // 뒤로가기 버튼 설정
        val arrowBtn102 = findViewById<ImageView>(R.id.btn_arrow102)
        arrowBtn102.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }



    private fun loadFavorites() {

        firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .get()
            .addOnSuccessListener { documents ->
                val favorites = documents.map { doc ->
                    Log.d("FavoritesActivity", "doc: ${doc.data}") // 각 문서 데이터 로그
                    FavoriteItem(
                        doc.id, // contentId
                        doc.getString("title") ?: "제목 없음",
                        doc.getString("image")
                    )
                }
                // 어댑터 생성 시 클릭 리스너 설정
                adapter = FavoritesAdapter(this, favorites.toMutableList()) { favorite ->
                    val intent = Intent(this, DetailActivity::class.java)
                    intent.putExtra("contentId", favorite.contentId) // 선택된 항목의 contentId 전달
                    startActivity(intent)
                }
                recyclerView.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "즐겨찾기 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
    }
}