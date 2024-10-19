package com.example.madagascar

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

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

        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)

        // 버튼을 동적으로 생성하고 그리드 레이아웃에 추가
        interests.forEach { interest ->
            val button = Button(this).apply {
                text = interest
                setBackgroundColor(Color.LTGRAY) // 초기 상태
                setOnClickListener {
                    if (selectedInterests.contains(interest)) {
                        selectedInterests.remove(interest)
                        setBackgroundColor(Color.LTGRAY) // 선택 해제 시 색상
                    } else {
                        selectedInterests.add(interest)
                        setBackgroundColor(Color.DKGRAY) // 선택 시 색상
                    }
                }
            }
            // 그리드 레이아웃에 버튼 추가
            gridLayout.addView(button)
        }

        // 확인 버튼 클릭 시 선택한 항목을 전달
        val confirmButton = findViewById<Button>(R.id.btn_confirm)
        confirmButton.setOnClickListener {
            val intent = Intent(this, NextActivity::class.java)
            intent.putStringArrayListExtra("selectedInterests", ArrayList(selectedInterests))
            startActivity(intent)
        }

        // 건너뛰기 클릭 시 다음 화면으로 이동
        val skipText = findViewById<TextView>(R.id.tv_skip)
        skipText.setOnClickListener {
            startActivity(Intent(this, NextActivity::class.java))
        }
    }
}
