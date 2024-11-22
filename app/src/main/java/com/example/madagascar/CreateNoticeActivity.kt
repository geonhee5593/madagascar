package com.example.madagascar

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class CreateNoticeActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_notice)

        firestore = FirebaseFirestore.getInstance()

        val edtNoticeMessage = findViewById<EditText>(R.id.edt_notice_message)
        val btnSendNotice = findViewById<Button>(R.id.btn_send_notice)

        btnSendNotice.setOnClickListener {
            val message = edtNoticeMessage.text.toString()

            if (message.isNotEmpty()) {
                sendNoticeToAllUsers(message)
            } else {
                Toast.makeText(this, "메시지를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendNoticeToAllUsers(message: String) {
        val noticeId = UUID.randomUUID().toString()

        // 서버 타임스탬프 사용
        val notice = mapOf(
            "id" to noticeId,
            "message" to message,
            "timestamp" to FieldValue.serverTimestamp() // 서버 타임스탬프
        )

        // Firestore에서 모든 유저 ID 가져오기
        firestore.collection("users").get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    // 유저의 id 필드 값을 사용해 공지사항 전송
                    sendNoticesToUsers(result.documents, notice)
                } else {
                    Toast.makeText(this, "등록된 유저가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "유저 목록을 불러오는 중 오류 발생", e)
                Toast.makeText(this, "유저 정보를 불러오지 못했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendNoticesToUsers(users: List<com.google.firebase.firestore.DocumentSnapshot>, notice: Map<String, Any>) {
        var successCount = 0
        var failureCount = 0

        for (userDoc in users) {
            val userIdField = userDoc.getString("id") // 유저의 id 필드 값
            Log.d(TAG, "공지 전송 대상 유저 ID: $userIdField")
            if (userIdField != null) {
                val userNoticeRef = firestore.collection("users").document(userDoc.id).collection("notices").document()

                userNoticeRef.set(notice)
                    .addOnSuccessListener {
                        successCount++
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
        val message = "공지사항 전송 완료: $successCount 성공, $failureCount 실패"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        if (failureCount == 0) finish()
    }

    companion object {
        private const val TAG = "CreateNoticeActivity"
    }
}
