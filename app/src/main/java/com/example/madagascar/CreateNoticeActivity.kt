package com.example.madagascar

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CreateNoticeActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_notice)

        // Firestore 초기화
        firestore = FirebaseFirestore.getInstance()

        val edtNoticeMessage = findViewById<EditText>(R.id.edt_notice_message)
        val btnSendNotice = findViewById<Button>(R.id.btn_send_notice)

        btnSendNotice.setOnClickListener {
            val message = edtNoticeMessage.text.toString()

            if (message.isNotEmpty()) {
                // 공지사항 전송 작업을 백그라운드에서 실행
                GlobalScope.launch(Dispatchers.IO) {
                    sendNoticeToAllUsers(message)
                }
            } else {
                Toast.makeText(this, "메시지를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendNoticeToAllUsers(message: String) {
        val noticeId = UUID.randomUUID().toString()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val notice = mapOf(
            "id" to noticeId,
            "message" to message,
            "timestamp" to timestamp
        )

        firestore.collection("users").get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    // 유저 ID 목록 추출
                    val userIds = result.documents.mapNotNull { it.id }
                    Log.d(TAG, "유저 ID 목록: $userIds")

                    // 각 유저의 notices 컬렉션에 공지사항 추가
                    sendNoticesToUsers(userIds, notice)
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "등록된 유저가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "유저 목록을 불러오는 중 오류 발생", e)
                runOnUiThread {
                    Toast.makeText(this, "유저 정보를 불러오지 못했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendNoticesToUsers(userIds: List<String>, notice: Map<String, String>) {
        val batch = firestore.batch()

        for (userId in userIds) {
            val userNoticeRef = firestore.collection("users").document(userId).collection("notices").document()
            batch.set(userNoticeRef, notice)
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d(TAG, "모든 공지사항 추가 성공")
                runOnUiThread {
                    showCompletionMessage(userIds.size, 0)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "공지사항 추가 실패", e)
                runOnUiThread {
                    showCompletionMessage(0, userIds.size)
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
