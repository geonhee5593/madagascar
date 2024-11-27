package com.example.madagascar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.Mypage.inquiries.InquiryAdapter
import com.example.madagascar.Mypage.inquiries.InquiryItem
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*

class AdminActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var inquiryAdapter: InquiryAdapter // RecyclerView 어댑터
    private var showUnanswered = true // 기본값: "답변 미확인 보기"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        firestore = FirebaseFirestore.getInstance()

        // RecyclerView 초기화
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewInquiries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        inquiryAdapter = InquiryAdapter { userId, inquiryId, newResponse ->
            respondToInquiry(userId, inquiryId, newResponse)
        }
        recyclerView.adapter = inquiryAdapter

        // 공지사항 작성 버튼
        val btnCreateNotice: Button = findViewById(R.id.btn_create_notice)
        btnCreateNotice.setOnClickListener {
            val intent = Intent(this, CreateNoticeActivity::class.java)
            startActivity(intent)
        }

        // 필터링 버튼
        val btnFilter: Button = findViewById(R.id.btn_filter)
        btnFilter.setOnClickListener {
            showUnanswered = !showUnanswered
            btnFilter.text = if (showUnanswered) "답변 완료 보기" else "답변 미확인 보기"
            fetchAllInquiries()
        }

        // 문의 데이터 로드
        fetchAllInquiries()
    }

    private fun fetchAllInquiries() {
        firestore.collection("users").get()
            .addOnSuccessListener { users ->
                val inquiries = mutableListOf<InquiryItem>()
                val tasks = mutableListOf<Task<QuerySnapshot>>()

                for (user in users) {
                    val userId = user.id
                    val task = firestore.collection("users")
                        .document(userId)
                        .collection("inquiries")
                        .whereEqualTo(
                            "responseStatus",
                            if (showUnanswered) "답변 미확인" else "답변 완료"
                        )
                        .get()
                        .addOnSuccessListener { inquiriesSnapshot ->
                            for (inquiry in inquiriesSnapshot) {
                                val inquiryData = inquiry.toObject(InquiryItem::class.java).apply {
                                    this.userId = userId
                                    this.inquiryId = inquiry.id
                                }
                                inquiries.add(inquiryData)
                            }
                        }
                    tasks.add(task)
                }

                Tasks.whenAllComplete(tasks).addOnCompleteListener {
                    Log.d("AdminActivity", "All inquiries loaded, count: ${inquiries.size}")
                    inquiryAdapter.submitList(inquiries)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "문의 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun respondToInquiry(userId: String, inquiryId: String, response: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        firestore.collection("users")
            .document(userId)
            .collection("inquiries")
            .document(inquiryId)
            .update(
                mapOf(
                    "adminResponse" to response,
                    "responseStatus" to "답변 완료",
                    "responseTimestamp" to timestamp
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this, "답변이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                fetchAllInquiries() // 목록 새로고침
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "답변 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
