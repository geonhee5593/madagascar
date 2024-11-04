package com.example.madagascar.Main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.madagascar.FavoritesActivity
import com.example.madagascar.Mylocation.fragmentActivity
import com.example.madagascar.Mypage.MypageActivity
import com.example.madagascar.R
import com.example.madagascar.happyguy.HappguyActivity
import com.example.madagascar.Hobby.hobby_Activity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/* 축모아 메인 화면 클래스 */
class MainActivity : AppCompatActivity() {
    /* Drawable 객체를 저장할 ArrayList 선언 */
    lateinit var temp: ArrayList<Drawable>
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private val imageList = listOf(
        R.drawable.image1,
        R.drawable.image2,
        R.drawable.image3,
        R.drawable.image4
    ) // 이미지 리소스
     /* onCreate 메서드, 액티비티가 생성될 때 호출됨 */
    @SuppressLint("MissingInflatedId")
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
            val intent = Intent(this, HappguyActivity::class.java)
            startActivity(intent)
        }

         /* btn_imageView4 버튼 클릭 시 내 위치 기반 축제 화면으로 이동 */
         val MylocationBtn = findViewById<ImageView>(R.id.btn_imageView4)
         MylocationBtn.setOnClickListener {
             val intent = Intent(this, fragmentActivity::class.java)
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
         viewPager2 = findViewById(R.id.viewPager2)
         tabLayout = findViewById(R.id.Tablayout)

         // ViewPager2와 어댑터 연결
         val adapter = FestivalImageAdapter(imageList)
         viewPager2.adapter = adapter

         // TabLayout과 ViewPager2 연결
         TabLayoutMediator(tabLayout, viewPager2) { _, _ -> }.attach()
    }
}
