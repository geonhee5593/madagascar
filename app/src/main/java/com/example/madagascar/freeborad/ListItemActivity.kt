package com.example.madagascar.freeborad

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.R

class ListItemActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listitem)  // 새로운 레이아웃 파일을 설정

        // 제목과 내용을 보여줄 TextView 초기화
        titleTextView = findViewById(R.id.titleTextView)
        contentTextView = findViewById(R.id.contentTextView)

        // 전달된 데이터 받기
        val title = intent.getStringExtra("title") ?: "No Title"
        val content = intent.getStringExtra("content") ?: "No Content"

        // 제목과 내용을 TextView에 설정
        titleTextView.text = title
        contentTextView.text = content
    }
}