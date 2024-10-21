package com.example.madagascar

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/* 축모아 메인 화면 클래스 */
class MainActivity : AppCompatActivity() {
    /* Drawable 객체를 저장할 ArrayList 선언 */
    lateinit var temp: ArrayList<Drawable>

     /* onCreate 메서드, 액티비티가 생성될 때 호출됨 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        /* btn_star1 버튼 클릭 시 즐겨찾기 화면으로 이동 */
        val favoritesBtn = findViewById<ImageView>(R.id.star1)
        favoritesBtn.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
        }

        /* btn_happhuy 버튼 클릭 시 월별 축제 화면으로 이동 */
        val happguyBtn = findViewById<ImageView>(R.id.btn_happguy)
        happguyBtn.setOnClickListener {
            val intent = Intent(this, happguyActivity::class.java)
            startActivity(intent)
        }

        /* btn_mypage1 버튼 클릭 시 마이페이지 화면으로 이동 */
        val mypageBtn = findViewById<ImageView>(R.id.btn_mypage1)
        mypageBtn.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

        /* btn_field 버튼 클릭 시 관심분야별 화면으로 이동 */
        val fieldBtn = findViewById<ImageView>(R.id.btn_field)
        fieldBtn.setOnClickListener {
            val intent = Intent(this, hobby_Activity::class.java)
            startActivity(intent)
        }
    }
}
