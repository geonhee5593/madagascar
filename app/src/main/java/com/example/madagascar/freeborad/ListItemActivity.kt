package com.example.madagascar.freeborad

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.R
import com.google.firebase.firestore.FirebaseFirestore

class ListItemActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var viewsTextView: TextView // 조회수 표시용 TextView
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var documentId: String = "" // Firestore 문서 ID
    private var views: Int = 0

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
        documentId = intent.getStringExtra("documentId") ?: ""

        if (documentId.isEmpty()) {
            Toast.makeText(this, "Invalid document ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 화면에 데이터 설정
        titleTextView.text = title
        contentTextView.text = content
        updateViews() // 조회수 초기화 및 표시

        // 뒤로가기 버튼 클릭 처리
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    // 조회수를 Firestore에 업데이트
    private fun updateViews() {
        views++ // 조회수 증가
        viewsTextView.text = "조회수: $views" // UI 업데이트

        // Firestore에 조회수 업데이트
        firestore.collection("FreeBoardItems")
            .document(documentId)
            .update("views", views)
            .addOnSuccessListener {
                Toast.makeText(this, "조회수가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "조회수 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}