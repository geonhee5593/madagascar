package com.example.madagascar

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NoticeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        val noticeContainer = findViewById<LinearLayout>(R.id.notice_container)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance().getReference("users/$currentUserId/notices")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                noticeContainer.removeAllViews() // 기존 공지사항 초기화

                for (noticeSnapshot in snapshot.children) {
                    val message = noticeSnapshot.child("message").getValue(String::class.java)
                    val timestamp = noticeSnapshot.child("timestamp").getValue(String::class.java)

                    if (message != null && timestamp != null) {
                        val textView = TextView(this@NoticeActivity).apply {
                            text = "$timestamp\n$message"
                            textSize = 16f
                            setPadding(16, 16, 16, 16)
                        }
                        noticeContainer.addView(textView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NoticeActivity", "공지사항 불러오기 실패", error.toException())
            }
        })
    }

}
