package com.example.madagascar

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.MypageActivity
import com.example.madagascar.R

class Notice : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        /* 공지사항 뒤로가기 코드 */

        val arrowBtn2 = findViewById<ImageView>(R.id.btn_arrow2)

        arrowBtn2.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }
    }
}
