package com.example.madagascar.Hobby

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.Main.MainActivity
import com.example.madagascar.R
import com.google.android.material.chip.ChipGroup

class hobby_Activity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chipGroup: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next)

        val arrowBtn5 = findViewById<ImageView>(R.id.btn_arrow5)

        arrowBtn5.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // SharedPreferences에서 선택된 관심사들을 불러옵니다.
        val sharedPreferences = getSharedPreferences("interests", Context.MODE_PRIVATE)
        val selectedInterests = sharedPreferences.getStringSet("selectedInterests", setOf()) ?: setOf()

        // 선택된 관심사들을 해시태그로 표시합니다.
        val hashtagText = selectedInterests.joinToString(" ") { "#$it" }
        findViewById<TextView>(R.id.tv_selected_interests).text = hashtagText


        // 리사이클러뷰 설정
        recyclerView = findViewById(R.id.recyclerViewContent)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2열로 그리드 레이아웃 설정



       /* // 예시 콘텐츠 데이터 (선택한 관심사에 따라 다르게 설정 가능)
        val contentList = getContentByInterest(selectedInterests)
        val adapter = ContentAdapter(contentList)
        recyclerView.adapter = adapter*/
    }

   /* // 선택된 관심사에 따라 다른 콘텐츠 반환하는 함수 (여기서는 임시 데이터 제공)
    private fun getContentByInterest(interests: ArrayList<String>?): List<Content> {
        val allContent = listOf(
            Content("음악", R.drawable.music_festival_1),
            Content("음악", R.drawable.music_festival_2),
            Content("영화", R.drawable.movie_festival_1)
            // 추가 콘텐츠 가능
        )
        return allContent.filter { interests?.contains(it.category) == true }
    }*/

}
