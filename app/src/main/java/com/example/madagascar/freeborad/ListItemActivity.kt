package com.example.madagascar.freeborad

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.R

class ListItemActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var viewsTextView: TextView // 조회수 표시용 TextView
    private var views: Int = 0
    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listitem)

        titleTextView = findViewById(R.id.titleTextView)
        contentTextView = findViewById(R.id.contentTextView)
        viewsTextView = findViewById(R.id.viewsTextView) // 조회수 TextView와 연결

        // 데이터 가져오기
        val title = intent.getStringExtra("title") ?: "No Title"
        val content = intent.getStringExtra("content") ?: "No Content"
        views = intent.getIntExtra("views", 0)
        position = intent.getIntExtra("position", -1)

        // position 검증
        if (position == -1) {
            Toast.makeText(this, "Invalid item position", Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        // 화면에 데이터 설정
        titleTextView.text = title
        contentTextView.text = content
        updateViews() // 조회수 초기화 및 표시

        // 뒤로가기 버튼 클릭 처리
        findViewById<Button>(R.id.backButton).setOnClickListener {
            returnResultAndFinish()
        }
    }

    // 조회수를 증가시키고 UI에 업데이트하는 함수
    private fun updateViews() {
        views++ // 조회수 증가
        viewsTextView.text = "조회수: $views" // UI 업데이트
        Toast.makeText(this, "조회수가 증가했습니다.", Toast.LENGTH_SHORT).show() // 알림
    }

    // 결과 반환 및 종료
    private fun returnResultAndFinish() {
        val resultIntent = Intent().apply {
            putExtra("updatedViews", views) // 증가된 조회수
            putExtra("position", position) // 클릭된 게시물의 position
        }
        setResult(RESULT_OK, resultIntent) // 결과 설정
        finish() // Activity 종료
    }
}