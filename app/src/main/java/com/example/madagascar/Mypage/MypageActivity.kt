package com.example.madagascar.Mypage

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.Main.MainActivity
import com.example.madagascar.R

class MypageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypageactivity)

        /* 공지사항으로 화면 전환 코드 */

        val noticeBtn = findViewById<ImageView>(R.id.btn_notice)

        noticeBtn.setOnClickListener {
            val intent = Intent(this, NoticeActivity::class.java)
            startActivity(intent)
        }

        /* 즐겨찾기로 화면 전환 코드 */

        val favoritesBtn = findViewById<ImageView>(R.id.btn_favorites)

        favoritesBtn.setOnClickListener {
            val intent = Intent(this, Favorites::class.java)
            startActivity(intent)
        }

        /* 개인 정보로 화면 전환 코드 */

        val HumanBtn = findViewById<ImageView>(R.id.btn_human)

        HumanBtn.setOnClickListener {
            val intent = Intent(this, Human::class.java)
            startActivity(intent)
        }

        val arrowbtn103 = findViewById<ImageView>(R.id.btn_arrow103)

        arrowbtn103.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}