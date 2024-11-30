package com.example.madagascar

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FindActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // 뷰 선언
    private lateinit var editTextEmail: EditText
    private lateinit var buttonResetPassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find)

        // "축모아" 텍스트에 그라데이션 적용
        val titleTextView: TextView = findViewById(R.id.app_title)
        val paint = titleTextView.paint
        val width = paint.measureText(titleTextView.text.toString())

        val shader = LinearGradient(
            0f, 0f, width, 0f,
            intArrayOf(
                getColor(R.color.deep_blue),  // 진한 파란색
                getColor(R.color.light_blue)  // 연한 파란색
            ),
            null,
            Shader.TileMode.CLAMP
        )
        titleTextView.paint.shader = shader

        // 뷰 연결
        editTextEmail = findViewById(R.id.editTextEmail)
        buttonResetPassword = findViewById(R.id.buttonResetPassword)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 비밀번호 재설정 버튼 클릭 리스너
        buttonResetPassword.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendPasswordResetEmail(email)
        }
    }

    // 비밀번호 재설정 이메일 전송
    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "비밀번호 재설정 이메일이 전송되었습니다. 이메일을 확인하세요.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "이메일 전송 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
