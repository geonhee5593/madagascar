package com.example.madagascar.Mypage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.Login
import com.example.madagascar.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Human : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_human)

        // Initialize UI elements
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)

        // Load user data
        loadUserInfo()

        /* 개인정보 뒤로가기 코드 */

        val arrowBtn101 = findViewById<ImageView>(R.id.btn_arrow101)

        arrowBtn101.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

        // Change password
        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        // Logout
        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        // Delete account
        btnDeleteAccount.setOnClickListener {
            deleteAccount()
        }
    }

    private fun loadUserInfo() {
        val user = auth.currentUser
        user?.let {
            tvEmail.text = user.email
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        tvName.text = document.getString("name") ?: "이름 없음"
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteAccount() {
        val user = auth.currentUser
        user?.let {
            // Re-authentication required before account deletion
            val credential = EmailAuthProvider.getCredential(user.email!!, "현재 비밀번호") // 비밀번호 필요
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    user.delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, Login::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "계정 삭제 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "재인증 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}