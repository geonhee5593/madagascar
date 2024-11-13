package com.example.madagascar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

    class listtextmadeActivity : AppCompatActivity() {
        private lateinit var titleEditText: EditText
        private lateinit var contentEditText: EditText
        private lateinit var saveButton: Button
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_listtextmade)

            // 레이아웃 컴포넌트 초기화
            titleEditText = findViewById(R.id.titleEditText9)
            contentEditText = findViewById(R.id.contentEditText9)
            saveButton = findViewById(R.id.saveButton9)

            // 저장 버튼 클릭 리스너
            saveButton.setOnClickListener {
                // 제목과 내용 가져오기
                val title = titleEditText.text.toString()
                val content = contentEditText.text.toString()

                if (title.isNotEmpty() && content.isNotEmpty()) {
                    // MainActivity로 데이터 전달
                    val intent = Intent()
                    intent.putExtra("title", title)
                    intent.putExtra("content", content)
                    setResult(RESULT_OK, intent)

                    // 현재 액티비티 종료
                    finish()
                }
            }
        }
    }