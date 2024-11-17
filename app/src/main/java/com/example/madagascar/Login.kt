package com.example.madagascar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.Hobby.hobby
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
    }

    private fun loginUser() {
        val id = findViewById<EditText>(R.id.Email_id).text.toString()
        val password = findViewById<EditText>(R.id.Password).text.toString()

        if (id == "admin" && password == "admin") {
            Toast.makeText(this, "관리자로 로그인되었습니다.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("isAdmin", true) // 관리자 여부 전달
            startActivity(intent)
            finish()
        } else {
            firestore.collection("users")
                .whereEqualTo("id", id)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(this, "로그인 실패: 잘못된 ID 또는 비밀번호", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("isAdmin", false) // 일반 사용자 여부 전달
                        startActivity(intent)
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "로그인 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Login", "로그인 오류", e)
                }
        }
    }

}
