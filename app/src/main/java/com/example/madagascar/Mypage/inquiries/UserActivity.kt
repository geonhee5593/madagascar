package com.example.madagascar.Mypage.inquiries

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class UserActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var inquiryAdapter: InquiryAdapter
    private var showUnanswered = true // 기본값: "답변 미확인" 보기

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        firestore = FirebaseFirestore.getInstance()

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewInquiries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        inquiryAdapter = InquiryAdapter(
            { _, _, _ -> }, // 유저 화면에서는 답변 이벤트 필요 없음
            showUnanswered,
            true // 유저 화면 플래그
        )
        recyclerView.adapter = inquiryAdapter

        // 문의 보내기 버튼
        val sendInquiryButton: Button = findViewById(R.id.buttonSendInquiry)
        sendInquiryButton.setOnClickListener {
            val inquiryText = findViewById<EditText>(R.id.editTextInquiry).text.toString()
            if (inquiryText.isNotEmpty()) {
                sendInquiry(inquiryText)
            } else {
                Toast.makeText(this, "문의 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // "답변 미확인/답변 완료" 버튼
        val toggleFilterButton: Button = findViewById(R.id.buttonToggleFilter)
        toggleFilterButton.setOnClickListener {
            showUnanswered = !showUnanswered
            toggleFilterButton.text = if (showUnanswered) "답변 완료 보기" else "답변 미확인 보기"
            fetchUserInquiries()
        }

        // 유저의 문의 목록 로드
        fetchUserInquiries()
    }

    private fun sendInquiry(question: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val timestamp = SimpleDateFormat("문의 보낸 시간 : "+"yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val inquiryData = hashMapOf(
            "question" to question,
            "timestamp" to timestamp,
            "adminResponse" to null,
            "responseStatus" to "답변 미확인"
        )

        firestore.collection("users")
            .document(userId)
            .collection("inquiries")
            .add(inquiryData)
            .addOnSuccessListener {
                Toast.makeText(this, "문의가 전송되었습니다.", Toast.LENGTH_SHORT).show()
                fetchUserInquiries() // 새로고침
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "문의 전송 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun fetchUserInquiries() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // 유저 정보 가져오기
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { userSnapshot ->
                val username = userSnapshot.getString("username") ?: "알 수 없음"
                firestore.collection("users")
                    .document(userId)
                    .collection("inquiries")
                    .whereEqualTo(
                        "responseStatus",
                        if (showUnanswered) "답변 미확인" else "답변 완료"
                    )
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val inquiries = querySnapshot.documents.mapNotNull { document ->
                            document.toObject(InquiryItem::class.java)?.apply {
                                this.userId = userId
                                this.username = username // 닉네임 설정
                                this.inquiryId = document.id
                            }
                        }
                        inquiryAdapter.submitList(inquiries)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "문의 목록을 불러오는 데 실패했습니다: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
    }
}

