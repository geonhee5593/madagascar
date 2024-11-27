package com.example.madagascar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.Hobby.Hobby
import com.example.madagascar.Main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore // Firestore 인스턴스 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firestore 인스턴스 초기화
        firestore = FirebaseFirestore.getInstance()

        val registerButton: Button = findViewById(R.id.registerbutton)
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()

        val loginButton: Button = findViewById(R.id.loginbutton)
        loginButton.setOnClickListener {
            loginUser()
        }
        // 새로 추가된 아이디/비밀번호 찾기 버튼
        val findButton: Button = findViewById(R.id.findButton)
        findButton.setOnClickListener {
            val intent = Intent(this, FindActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {
        val id = findViewById<EditText>(R.id.Email_id).text.toString()
        val password = findViewById<EditText>(R.id.Password).text.toString()

        firestore.collection("users")
            .whereEqualTo("id", id)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "로그인 실패: 잘못된 ID 또는 비밀번호", Toast.LENGTH_SHORT).show()
                } else {
                    val document = documents.first()
                    val uid = document.id

                    // Firestore에서 isFirstLogin 및 isAdmin 확인
                    firestore.collection("users").document(uid)
                        .get()
                        .addOnSuccessListener { userDocument ->
                            val isFirstLogin = userDocument.getBoolean("isFirstLogin") ?: true
                            val isAdmin = userDocument.getBoolean("isAdmin") ?: false

                            if (isAdmin) {
                                // 관리자로 로그인
                                Toast.makeText(this, "관리자로 로그인되었습니다.", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("isAdmin", true) // 관리자 여부 전달
                                intent.putExtra("uid", uid) // 사용자 UID 전달
                                startActivity(intent)
                                finish()
                            } else if (isFirstLogin) {
                                // 관심 분야 선택 화면으로 이동
                                Toast.makeText(this, "첫 로그인: 관심 분야 선택 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, Hobby::class.java)
                                intent.putExtra("uid", uid) // 사용자 UID 전달
                                startActivity(intent)
                                finish()
                            } else {
                                // 메인 화면으로 이동
                                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("isAdmin", false) // 일반 사용자 여부 전달
                                intent.putExtra("uid", uid) // 사용자 UID 전달
                                startActivity(intent)
                                finish()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "사용자 정보 확인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("Login", "사용자 정보 확인 실패", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Firestore 조회 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Login", "Firestore 조회 오류", e)
            }
    }







}
