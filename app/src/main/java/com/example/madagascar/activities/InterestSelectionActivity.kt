package com.example.madagascar.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity // AppCompatActivity 임포트
import android.widget.Button
import android.widget.GridLayout // GridLayout 임포트
import android.graphics.Color
import android.util.Log
import com.example.madagascar.R

class InterestSelectionActivity : AppCompatActivity() {

    private lateinit var selectedInterests: MutableSet<String>
    private val interests = listOf(
        "Music", "Food", "Art", "Fashion", "Sports",
        "Movies", "Literature", "Drama", "Games", "Culture",
        "Animals", "Healing", "Environment", "Travel", "Beauty"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interest_selection)

        selectedInterests = mutableSetOf()

        val gridLayout = findViewById<GridLayout>(R.id.grid_layout_interests)
        gridLayout.columnCount = 3 // 3열로 설정

        interests.forEach { interest ->
            val button = createInterestButton(interest)
            gridLayout.addView(button)
        }

        // 확인 버튼 클릭 시 선택된 관심 분야 확인
        findViewById<Button>(R.id.button_confirm).setOnClickListener {
            // 선택한 관심사를 처리하는 로직 추가
            Log.d("SelectedInterests", selectedInterests.toString())
        }

        // 건너뛰기 버튼 처리
        findViewById<Button>(R.id.button_skip).setOnClickListener {
            // 건너뛰기 버튼 처리 로직 추가 (ex: 다음 화면으로 이동)
        }
    }

    private fun createInterestButton(interest: String): Button {
        val button = Button(this).apply {
            text = interest
            setBackgroundResource(R.drawable.selector_interest_button) // 버튼 스타일 적용
            setTextColor(Color.BLACK)
            setPadding(16, 16, 16, 16)
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(8, 8, 8, 8)
            }

            setOnClickListener {
                // 선택 시 색상 변경 로직
                if (selectedInterests.contains(interest)) {
                    selectedInterests.remove(interest)
                    setBackgroundColor(Color.WHITE) // 선택 해제 시 흰색으로
                } else {
                    selectedInterests.add(interest)
                    setBackgroundColor(Color.GRAY) // 선택 시 회색으로
                }
            }
        }
        return button
    }
}
