package com.example.madagascar.Mypage.inquiries

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class UserActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        firestore = FirebaseFirestore.getInstance()

        val sendInquiryButton: Button = findViewById(R.id.buttonSendInquiry)
        sendInquiryButton.setOnClickListener {
            val inquiryText = findViewById<EditText>(R.id.editTextInquiry).text.toString()
            if (inquiryText.isNotEmpty()) {
                sendInquiry(inquiryText)
            } else {
                Toast.makeText(this, "문의 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendInquiry(question: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

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
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "문의 전송 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
