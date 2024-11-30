package com.example.madagascar.Main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.madagascar.API.DetailActivity
import com.example.madagascar.API.Festival
import com.example.madagascar.API.FestivalResponse
import com.example.madagascar.API.MonthFestival
import com.example.madagascar.API.RegionFestival
import com.example.madagascar.API.RetrofitClient
import com.example.madagascar.AdminActivity
import com.example.madagascar.Hobby.Hobby_Activity
import com.example.madagascar.Mylocation.fragmentActivity
import com.example.madagascar.Mypage.Favorites
import com.example.madagascar.Mypage.MypageActivity
import com.example.madagascar.R
import com.example.madagascar.freeborad.FreeBoradActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* 축모아 메인 화면 클래스 */
class MainActivity : AppCompatActivity() {
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private val handler = Handler(Looper.getMainLooper())

    /* onCreate 메서드, 액티비티가 생성될 때 호출됨 */
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "MainActivity onCreate started")
        setContentView(R.layout.activity_main)

        // "축모아" 텍스트에 그라데이션 적용
        val titleTextView: TextView = findViewById(R.id.btn)
        val paint = titleTextView.paint
        val width = paint.measureText(titleTextView.text.toString())

        val shader = LinearGradient(
            0f, 0f, width, 0f,
            intArrayOf(
                getColor(R.color.deep_blue),  // 진한 파란색
                getColor(R.color.light_blue) // 연한 파란색
            ),
            null,
            Shader.TileMode.CLAMP
        )
        titleTextView.paint.shader = shader


        /* 관리자 버튼 초기화 */
        val adminButton = findViewById<Button>(R.id.btn_admin)
        adminButton.visibility = View.GONE // 기본값으로 숨김 처리

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Firestore에서 현재 사용자 정보를 가져옴
            val uid = currentUser.uid
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val isAdmin = document.getBoolean("isAdmin") ?: false
                        if (isAdmin) {
                            adminButton.visibility = View.VISIBLE
                            adminButton.setOnClickListener {
                                val intent = Intent(this, AdminActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    } else {
                        Log.e("MainActivity", "사용자 문서가 존재하지 않습니다.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Firestore에서 사용자 정보 가져오기 실패: ${e.message}")
                }
        } else {
            Log.e("MainActivity", "사용자가 로그인되지 않았습니다.")
        }


        // 검색 버튼 동작 설정
        setupSearch()

        // 버튼 클릭 리스너 설정
        setupButtons()

        // ViewPager2와 TabLayout 초기화
        viewPager2 = findViewById(R.id.viewPager2)
        tabLayout = findViewById(R.id.Tablayout)

        // 축제 데이터 가져오기
        fetchFestivals()
    }
    private fun setupButtons() {
        findViewById<ImageView>(R.id.star1).setOnClickListener {
            Toast.makeText(this, "즐겨찾기 버튼 클릭됨", Toast.LENGTH_SHORT).show()
        }

        /* 기존 버튼 동작 코드 유지 */
        /* btn_star1 버튼 클릭 시 즐겨찾기 화면으로 이동 */
        val favoritesBtn = findViewById<ImageView>(R.id.star1)
        favoritesBtn.setOnClickListener {
            try {
                val intent = Intent(this, Favorites::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "즐겨찾기 화면으로 이동 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        /* btn_imageView4 버튼 클릭 시 내 위치 기반 축제 화면으로 이동 */
        val MylocationBtn = findViewById<ImageView>(R.id.btn_imageView4)
        MylocationBtn.setOnClickListener {
            Log.d("fragmentActivity", "onClick: Location button clicked")
            val intent = Intent(this, fragmentActivity::class.java)
            startActivity(intent)
        }

        val festivalbtn = findViewById<ImageView>(R.id.festivalIcon)
        festivalbtn.setOnClickListener {
            val intent = Intent(this, Festival::class.java)
            startActivity(intent)
        }

        val mypagebtn = findViewById<ImageView>(R.id.mypageIcon)
        mypagebtn.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
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

        /* btn_field 버튼 클릭 시 관심분야별 화면으로 이동 */
        val fieldBtn = findViewById<ImageView>(R.id.btn_field)
        fieldBtn.setOnClickListener {
            val intent = Intent(this, Hobby_Activity::class.java)
            startActivity(intent)
        }
        val freeBoradbtn = findViewById<ImageView>(R.id.imageView)
        freeBoradbtn.setOnClickListener {
            val intent = Intent(this, FreeBoradActivity::class.java)
            startActivity(intent)

        }
    }

    private fun fetchFestivals() {
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        // Retrofit으로 API 호출
        RetrofitClient.instance.getFestivals().enqueue(object : Callback<FestivalResponse> {
            override fun onResponse(call: Call<FestivalResponse>, response: Response<FestivalResponse>) {
                if (response.isSuccessful) {
                    val festivals = response.body()?.response?.body?.items?.item ?: emptyList()

                    // 가까운 4개의 축제 필터링
                    val upcomingFestivals = festivals
                        .filter { it.eventStartDate >= currentDate }
                        .sortedBy { it.eventStartDate }
                        .take(4)

                    if (upcomingFestivals.isNotEmpty()) {
                        val adapter = FestivalSliderAdapter(this@MainActivity, upcomingFestivals)
                        viewPager2.adapter = adapter

                        // 자동 슬라이드 설정
                        setupAutoSlide(upcomingFestivals.size)

                        TabLayoutMediator(tabLayout, viewPager2) { _, _ -> }.attach()
                    } else {
                        Toast.makeText(this@MainActivity, "표시할 축제가 없습니다.", Toast.LENGTH_SHORT).show()
                        viewPager2.visibility = View.GONE
                    }
                } else {
                    Log.e("MainActivity", "API 호출 실패: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                Log.e("MainActivity", "API 호출 오류: ${t.message}")
            }
        })
    }

    private fun setupSearch() {
        try {
            val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.search_view)
            val searchButton = findViewById<Button>(R.id.search_button_main)

            searchButton.setOnClickListener {
                val query = searchView.query.toString().trim()
                if (query.isNotEmpty()) {
                    val intent = Intent(this, SearchResultsActivity::class.java)
                    intent.putExtra("searchQuery", query) // 검색어 전달
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up search: ${e.message}")
            Toast.makeText(this, "검색 기능 설정 중 오류 발생", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupAutoSlide(itemCount: Int) {
        val runnable = object : Runnable {
            override fun run() {
                val currentItem = viewPager2.currentItem
                val nextItem = (currentItem + 1) % itemCount
                viewPager2.currentItem = nextItem
                handler.postDelayed(this, 3000) // 3초마다 슬라이드
            }
        }
        handler.postDelayed(runnable, 3000)

        // 어댑터에 클릭 리스너 설정
        if (viewPager2.adapter is FestivalSliderAdapter) {
            val adapter = viewPager2.adapter as FestivalSliderAdapter
            adapter.setOnItemClickListener { festival ->
                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                intent.putExtra("contentId", festival.contentId)
                intent.putExtra("previous_screen", "MainActivity")
                startActivity(intent)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // 핸들러 정리
    }

}

