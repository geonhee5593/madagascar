package com.example.madagascar.Mypage

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        // UI 요소 초기화
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)

        // 비밀번호 변경 버튼 클릭 리스너
        btnChangePassword.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        val currentPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // 입력값 검증
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid // 현재 사용자 UID를 가져옴
            val userDocumentRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            // Firestore에서 현재 비밀번호 확인
            userDocumentRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val storedPassword = document.getString("password") ?: ""
                        if (storedPassword == currentPassword) {
                            // 비밀번호가 일치하면 Firestore에 새 비밀번호 업데이트
                            userDocumentRef.update("password", newPassword)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                                    finish() // 화면 닫기
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "비밀번호 변경 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "현재 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Firestore 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "사용자 정보가 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateFirestorePassword(userId: String, newPassword: String) {
        db.collection("users").document(userId)
            .update("password", newPassword)
            .addOnSuccessListener {
                Toast.makeText(this, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Firestore 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
