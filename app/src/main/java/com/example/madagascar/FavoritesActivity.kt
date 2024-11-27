package com.example.madagascar

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.Main.MainActivity

class FavoritesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        val favoritesBtn = findViewById<ImageView>(R.id.btn_arrow102)

        favoritesBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java) // 'FavoritesActivity'로 수정
            startActivity(intent)
        }

    }

}

