package com.example.madagascar

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.Hobby.Hobby
import com.example.madagascar.Main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val registerButton: Button = findViewById(R.id.registerbutton)
        // "축모아" 텍스트에 그라데이션 적용
        val titleTextView: TextView = findViewById(R.id.app_title)
        val paint = titleTextView.paint
        val width = paint.measureText(titleTextView.text.toString())
        val shader = LinearGradient(
            0f, 0f, width, 0f,
            intArrayOf(
                getColor(R.color.deep_blue),
                getColor(R.color.light_blue)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        titleTextView.paint.shader = shader

        val loginButton: Button = findViewById(R.id.loginbutton)
        loginButton.setOnClickListener {
            loginUser()
        }
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {
        val email = findViewById<EditText>(R.id.Email_id).text.toString()
        val password = findViewById<EditText>(R.id.Password).text.toString()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase Auth로 로그인
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        if (!user.isEmailVerified) {
                            // 이메일 인증 여부 확인
                            Toast.makeText(this, "이메일 인증을 완료하세요.", Toast.LENGTH_SHORT).show()
                            auth.signOut() // 인증되지 않은 사용자 로그아웃
                        } else {
                            // Firestore에서 추가 정보 확인
                            checkUserFirstLogin(user.uid)
                        }
                    }
                } else {
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserFirstLogin(uid: String) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val isFirstLogin = document.getBoolean("isFirstLogin") ?: true

                if (isFirstLogin) {
                    Toast.makeText(this, "첫 로그인: 관심 분야 설정으로 이동합니다.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Hobby::class.java)
                    intent.putExtra("uid", uid)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("uid", uid)
                    startActivity(intent)
                }
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Firestore 조회 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Login", "Firestore 조회 실패", e)
            }
    }
}
