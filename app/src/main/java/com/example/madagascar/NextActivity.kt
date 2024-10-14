package com.example.madagascar

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NextActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next)

        val selectedCategories = intent.getStringArrayListExtra("selectedCategories")
        val textView: TextView = findViewById(R.id.textView)

        // 선택한 카테고리들을 화면에 표시
        textView.text = "선택한 카테고리: ${selectedCategories?.joinToString(", ")}"
    }
}
