package com.example.madagascar.API

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.madagascar.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.bumptech.glide.request.transition.Transition // Transition 인터페이스를 사용하기 위한 import

class DetailActivity : AppCompatActivity() {
    private lateinit var titleView: TextView
    private lateinit var addressView: TextView
    private lateinit var overviewView: TextView
    private lateinit var imageView: ImageView
    private lateinit var eventDatesView: TextView
    private lateinit var telView: TextView
    private lateinit var priceView: TextView
    private lateinit var playtimeView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // 뷰 초기화
        initializeViews()

        // 뒤로가기 버튼 설정
        setupBackButton()

        // contentId를 받아 두 API 호출
        val contentId = intent.getStringExtra("contentId")
        if (contentId != null) {
            fetchDetailData(contentId)
        }
    }
        // 뷰 초기화
        private fun initializeViews() {
            titleView = findViewById(R.id.title)
            addressView = findViewById(R.id.address)
            overviewView = findViewById(R.id.overview)
            imageView = findViewById(R.id.image)
            eventDatesView = findViewById(R.id.event_dates)
            telView = findViewById(R.id.tel)
            priceView = findViewById(R.id.price)
            playtimeView = findViewById(R.id.playtime)
        }
    private fun setupBackButton() {
        val backButton = findViewById<ImageView>(R.id.btn_arrow120)
        backButton.setOnClickListener { handleBackNavigation() }
    }

    private fun handleBackNavigation() {
        val previousScreen = intent.getStringExtra("previous_screen")
        when (previousScreen) {
            "Festival" -> {
                val intent = Intent(this, Festival::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
            "RegionFestival" -> {
                val intent = Intent(this, RegionFestival::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
            else -> onBackPressedDispatcher.onBackPressed()
        }
    }


    private fun fetchDetailData(contentId: String) {
        // 공통 정보 호출
        val commonCall = RetrofitClient.instance.getCommon(contentId)
        // 소개 정보 호출
        val introCall = RetrofitClient.instance.getIntro(contentId)

        commonCall.enqueue(object : Callback<CommonResponse> {
            override fun onResponse(
                call: Call<CommonResponse>,
                response: Response<CommonResponse>
            ) {
                val commonItem = response.body()?.response?.body?.items?.item?.firstOrNull()
                if (commonItem != null) {
                    updateCommonData(commonItem)
                } else {
                    Toast.makeText(this@DetailActivity, "공통 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "공통 정보 호출 실패", Toast.LENGTH_SHORT).show()
            }
        })

        introCall.enqueue(object : Callback<IntroResponse> {
            override fun onResponse(call: Call<IntroResponse>, response: Response<IntroResponse>) {
                val introItem = response.body()?.response?.body?.items?.item?.firstOrNull()
                if (introItem != null) {
                    updateIntroData(introItem)
                } else {
                    Toast.makeText(this@DetailActivity, "소개 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<IntroResponse>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "소개 정보 호출 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateCommonData(commonItem: CommonItem) {
        titleView.text = commonItem.title
        addressView.text = commonItem.addr1 ?: "주소 정보 없음"
        overviewView.text = commonItem.overview ?: "개요 정보 없음"
        telView.text = commonItem.tel ?: "연락처 정보 없음"

        val startDate = formatDate(commonItem.eventStartDate)
        val endDate = formatDate(commonItem.eventEndDate)
        eventDatesView.text = "기간: $startDate ~ $endDate"

        val imageUrl = commonItem.firstimage
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this@DetailActivity)
                .asBitmap() // 비트맵으로 로드
                .load(commonItem.firstimage)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val width = resource.width
                        val height = resource.height
                        if (isPoster(width, height)) {
                            // 포스터로 처리
                            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                            imageView.layoutParams.height = resources.displayMetrics.heightPixels
                        } else {
                            // 일반 이미지로 처리
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                            imageView.layoutParams.height = resources.displayMetrics.widthPixels * height / width
                        }
                        imageView.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Placeholder 설정 가능
                        imageView.setImageDrawable(placeholder)
                    }
                })
        }
    }

    private fun isPoster(width: Int, height: Int): Boolean {
        val ratio = height.toFloat() / width.toFloat()
        return ratio > 1.2 // 세로가 가로보다 1.2배 이상 크면 포스터로 간주
    }

    private fun updateIntroData(introItem: IntroItem) {
        priceView.text = "가격: ${introItem.usetimefestival ?: "정보 없음"}"
        playtimeView.text = "운영 시간: ${introItem.playtime ?: "정보 없음"}"
    }

    // 날짜 포맷팅 함수
    private fun formatDate(date: String?): String {
        return try {
            if (date.isNullOrEmpty() || date.length != 8) return "날짜 정보 없음"
            val originalFormat = DateTimeFormatter.ofPattern("yyyyMMdd")
            val targetFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd")
            val parsedDate = LocalDate.parse(date, originalFormat)
            targetFormat.format(parsedDate)
        } catch (e: Exception) {
            "날짜 정보 없음"
        }
    }
}