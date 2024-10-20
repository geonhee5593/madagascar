package com.example.madagascar

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    lateinit var  temp : ArrayList<Drawable>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val favoritesBtn = findViewById<ImageView>(R.id.star1) //즐겨찾기 이동

        favoritesBtn.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
        }

        val happguyBtn = findViewById<ImageView>(R.id.btn_happguy)

        happguyBtn.setOnClickListener {
            val intent = Intent(this, happguyActivity::class.java)
            startActivity(intent)
        }

        val mypageBtn = findViewById<ImageView>(R.id.btn_mypage1)

        mypageBtn.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }
        val fieldBtn = findViewById<ImageView>(R.id.btn_field)

        fieldBtn.setOnClickListener {
            val intent = Intent(this, hobby_Activity::class.java)
            startActivity(intent)
        }
    }
}


