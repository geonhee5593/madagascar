package com.example.madagascar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.R

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val btnCreateNotice = findViewById<Button>(R.id.btn_create_notice)

        // 공지사항 작성 화면으로 이동
        btnCreateNotice.setOnClickListener {
            val intent = Intent(this, CreateNoticeActivity::class.java)
            startActivity(intent)
        }
    }
}