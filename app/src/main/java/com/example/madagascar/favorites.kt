package com.example.madagascar

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.MypageActivity
import com.example.madagascar.R

class favorites : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        /* 즐겨찾기 뒤로가기 코드 */

        val arrowBtn3 = findViewById<ImageView>(R.id.btn_arrow3)

        arrowBtn3.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }
    }
}
