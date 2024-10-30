package com.example.madagascar.Hobby

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.Main.MainActivity
import com.example.madagascar.R

class hobby : AppCompatActivity() {

    private val selectedInterests = mutableListOf<String>()
    private val interests = listOf(
        "음악", "음식", "예술", "패션", "스포츠", "영화",
        "문학", "드라마", "게임", "문화 체험", "동물", "힐링",
        "환경", "여행", "뷰티"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hobby_screen)

        val sharedPreferences = getSharedPreferences("interests", Context.MODE_PRIVATE)
        val savedInterests = sharedPreferences.getStringSet("selectedInterests", setOf())?.toMutableSet()

        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)

        // 동적으로 버튼을 생성하고 GridLayout에 추가
        interests.forEach { interest ->
            val button = Button(this).apply {
                text = interest
                setBackgroundColor(Color.LTGRAY) // 초기 상태
                setOnClickListener {
                    if (selectedInterests.contains(interest)) {
                        selectedInterests.remove(interest)
                        setBackgroundColor(Color.LTGRAY) // 선택 해제 시 색상 변경
                    } else {
                        selectedInterests.add(interest)
                        setBackgroundColor(Color.DKGRAY) // 선택 시 색상 변경
                    }
                }
            }
            gridLayout.addView(button)
        }

        // 확인 버튼 클릭 시 선택한 항목과 전체 관심사 리스트를 전달
        val confirmButton = findViewById<Button>(R.id.btn_confirm)
        confirmButton.setOnClickListener {
            sharedPreferences.edit().putStringSet("selectedInterests", selectedInterests.toSet()).apply()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 건너뛰기 클릭 시 메인 화면으로 이동
        val skipText = findViewById<TextView>(R.id.tv_skip)
        skipText.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
