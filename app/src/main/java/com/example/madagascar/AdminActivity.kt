package com.example.madagascar

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.madagascar.Mypage.inquiries.InquiryAdapter
import com.example.madagascar.Mypage.inquiries.InquiryItem
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import org.json.JSONObject
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

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewInquiries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        inquiryAdapter = InquiryAdapter(
            { userId, inquiryId, newResponse -> respondToInquiry(userId, inquiryId, newResponse) },
            showUnanswered,
            false // 관리자 화면 플래그
        )
        recyclerView.adapter = inquiryAdapter

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
                    val username = user.getString("username") ?: "알 수 없음" // 사용자 이름 가져오기
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
                                    this.username = username // 사용자 이름 설정
                                    this.inquiryId = inquiry.id
                                }
                                inquiries.add(inquiryData)
                            }
                        }
                    tasks.add(task)
                }

                Tasks.whenAllComplete(tasks).addOnCompleteListener {
                    inquiries.sortByDescending { it.timestamp } // 최신순으로 정렬
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
    private fun sendNoticesToUsers(users: List<DocumentSnapshot>, notice: Map<String, Any>) {
        Log.d(TAG, "sendNoticesToUsers 호출됨. 대상 유저 수: ${users.size}")
        var successCount = 0
        var failureCount = 0

        for (userDoc in users) {
            val userIdField = userDoc.getString("id") // 유저의 id 필드 값
            val fcmToken = userDoc.getString("fcmToken") // FCM 토큰 가져오기

            if (userIdField != null) {
                val userNoticeRef = firestore.collection("users").document(userDoc.id)
                    .collection("notices").document()

                userNoticeRef.set(notice)
                    .addOnSuccessListener {
                        successCount++
                        if (fcmToken != null) {
                            sendPushNotification(fcmToken, "새 공지사항", notice["message"] as String)
                        }
                        if (successCount + failureCount == users.size) {
                            showCompletionMessage(successCount, failureCount)
                        }
                    }
                    .addOnFailureListener { e ->
                        failureCount++
                        Log.e(TAG, "유저 $userIdField 에게 공지 전송 실패", e)
                        if (successCount + failureCount == users.size) {
                            showCompletionMessage(successCount, failureCount)
                        }
                    }
            } else {
                failureCount++
                Log.e(TAG, "유저 문서에 id 필드가 없음")
                if (successCount + failureCount == users.size) {
                    showCompletionMessage(successCount, failureCount)
                }
            }
        }
    }
    private fun showCompletionMessage(successCount: Int, failureCount: Int) {
        val message = "공지 전송 완료: 성공 $successCount 개, 실패 $failureCount 개"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.d(TAG, message)
    }


    private fun sendPushNotification(token: String, title: String, body: String) {
        val message = mapOf(
            "to" to token,
            "notification" to mapOf(
                "title" to title,
                "body" to body
            )
        )

        // FCM REST API 사용
        val url = "https://fcm.googleapis.com/fcm/send"
        val jsonObject = JSONObject(message)
        val jsonBody = jsonObject.toString()

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                Log.d(TAG, "푸시 알림 전송 성공: $response")
            },
            { error ->
                Log.e(TAG, "푸시 알림 전송 실패", error)
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = "key=AIzaSyAvYHk-xFrYM5F8CjD0tGELetc3P_27fZ4" // Firebase Console의 서버 키
                return headers
            }

            override fun getBody(): ByteArray {
                return jsonBody.toByteArray()
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

}
