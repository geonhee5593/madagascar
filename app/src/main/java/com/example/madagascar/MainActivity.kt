package com.example.madagascar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InterestAdapter
    private val interests = listOf(
        "음악", "음식", "예술", "패션", "스포츠", "영화", "문학", "드라마",
        "게임", "문화체험", "동물", "힐링", "환경", "여행", "뷰티"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        val confirmButton: Button = findViewById(R.id.btn_confirm)
        val skipTextView: TextView = findViewById(R.id.tv_skip)

        adapter = InterestAdapter(interests)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // '확인' 버튼 클릭 시 선택한 항목을 가지고 다음 화면으로 이동
        confirmButton.setOnClickListener {
            val selectedInterests = adapter.getSelectedInterests()
            val intent = Intent(this, NextActivity::class.java)
            intent.putStringArrayListExtra("selectedCategories", ArrayList(selectedInterests))
            startActivity(intent)
        }

        // '건너뛰기' 클릭 시 아무 데이터 없이 다음 화면으로 이동
        skipTextView.setOnClickListener {
            startActivity(Intent(this, NextActivity::class.java))
        }
    }
}
