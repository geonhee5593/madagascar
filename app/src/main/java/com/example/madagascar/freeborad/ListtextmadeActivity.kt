package com.example.madagascar.freeborad

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.R

class ListtextmadeActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listtextmade)

        // EditText 초기화
        titleEditText = findViewById(R.id.titleEditText9)
        contentEditText = findViewById(R.id.contentEditText9)
        saveButton = findViewById(R.id.saveButton9)

        // 전달된 데이터 받기
        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""

        // EditText에 제목과 내용 채우기
        titleEditText.setText(title)
        contentEditText.setText(content)

        // 저장 버튼 클릭 리스너
        saveButton.setOnClickListener {
            val newTitle = titleEditText.text.toString()
            val newContent = contentEditText.text.toString()

            // 새 제목과 내용을 FreeBoradActivity로 전달
            val resultIntent = Intent()
            resultIntent.putExtra("title", newTitle)
            resultIntent.putExtra("content", newContent)
            setResult(RESULT_OK, resultIntent) // 결과를 FreeBoradActivity로 전달
            finish() // ListtextmadeActivity 종료하고 이전 화면으로 돌아가기
        }
    }
}