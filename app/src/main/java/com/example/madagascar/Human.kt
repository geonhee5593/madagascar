package com.example.madagascar

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.MypageActivity
import com.example.madagascar.R

class Human : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_human)

        /* 개인정보 뒤로가기 코드 */

        val arrowBtn4 = findViewById<ImageView>(R.id.btn_arrow4)

        arrowBtn4.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }
    }
}