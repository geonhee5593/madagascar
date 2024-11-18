package com.example.madagascar

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        val database = FirebaseDatabase.getInstance("https://your-database-name.firebaseio.com").getReference("notices")
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val noticeId = database.push().key

        val notice = mapOf(
            "message" to message,
            "timestamp" to timestamp
        )

        // 모든 사용자에게 공지사항 추가
        firestore.collection("users").get()
            .addOnSuccessListener { users ->
                if (!users.isEmpty) {
                    for (user in users) {
                        val userId = user.id
                        val userNoticeRef = FirebaseDatabase.getInstance()
                            .getReference("users/$userId/notices")
                            .child(noticeId!!)

                        userNoticeRef.setValue(notice)
                            .addOnSuccessListener {
                                Log.d("CreateNoticeActivity", "공지사항 저장 성공: $userId")
                            }
                            .addOnFailureListener { e ->
                                Log.e("CreateNoticeActivity", "공지사항 저장 실패", e)
                            }
                    }
                } else {
                    Log.d("CreateNoticeActivity", "사용자 없음")
                }
            }
            .addOnFailureListener { e ->
                Log.e("CreateNoticeActivity", "Firestore 사용자 조회 실패", e)
            }
    }



}
