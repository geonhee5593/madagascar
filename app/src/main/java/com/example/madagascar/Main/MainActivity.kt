package com.example.madagascar.Main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.madagascar.API.Festival
import com.example.madagascar.API.MonthFestival
import com.example.madagascar.API.RegionFestival
import com.example.madagascar.AdminActivity
import com.example.madagascar.FavoritesActivity
import com.example.madagascar.freeborad.FreeBoradActivity
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
         Log.d("MainActivity", "MainActivity onCreate started")
        setContentView(R.layout.activity_main)

         /* 관리자 버튼 초기화 */
         val adminButton = findViewById<Button>(R.id.btn_admin)
         // 로그인 액티비티에서 전달된 관리자 여부 확인
         val isAdmin = intent.getBooleanExtra("isAdmin", true)
         if (isAdmin) {
             adminButton.visibility = View.VISIBLE
             adminButton.setOnClickListener {
                 val intent = Intent(this, AdminActivity::class.java)
                 startActivity(intent)
             }
         } else {
             adminButton.visibility = View.GONE
         }
         /* 기존 버튼 동작 코드 유지 */
        /* btn_star1 버튼 클릭 시 즐겨찾기 화면으로 이동 */
        val favoritesBtn = findViewById<ImageView>(R.id.star1)
        favoritesBtn.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
        }

         /* btn_imageView4 버튼 클릭 시 내 위치 기반 축제 화면으로 이동 */
         val MylocationBtn = findViewById<ImageView>(R.id.btn_imageView4)
         MylocationBtn.setOnClickListener {
             val intent = Intent(this, fragmentActivity::class.java)
             startActivity(intent)
         }

         /* btn_region 버튼 클릭 시 지역 별 축제 화면으로 이동 */
         val regionbtn = findViewById<ImageView>(R.id.btn_region)
         regionbtn.setOnClickListener {
             val intent = Intent(this, RegionFestival::class.java)
             startActivity(intent)
         }
         /* btn_month 버튼 클릭 시 월 별 축제 화면으로 이동 */
         val monthbtn = findViewById<ImageView>(R.id.btn_month)
         monthbtn.setOnClickListener {
             val intent = Intent(this, MonthFestival::class.java)
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

         val festivalbtn = findViewById<ImageView>(R.id.Festival)
         festivalbtn.setOnClickListener {
             val intent = Intent(this, Festival::class.java)
             startActivity(intent)
         }
         val freeBoradbtn = findViewById<ImageView>(R.id.imageView)
         freeBoradbtn.setOnClickListener {
             val intent = Intent(this, FreeBoradActivity::class.java)
             startActivity(intent)
         }
    }
}
