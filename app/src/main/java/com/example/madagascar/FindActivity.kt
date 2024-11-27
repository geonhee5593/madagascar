package com.example.madagascar

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class FindActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private var verificationId: String? = null

    // 뷰 선언
    private lateinit var editTextPhone: EditText
    private lateinit var editTextVerificationCode: EditText
    private lateinit var buttonSendCode: Button
    private lateinit var buttonVerifyCode: Button
    private lateinit var buttonFindId: Button
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
                getColor(R.color.light_blue) // 연한 파란색
            ),
            null,
            Shader.TileMode.CLAMP
        )
        titleTextView.paint.shader = shader
        // 뷰 연결
        editTextPhone = findViewById(R.id.editTextPhone)
        editTextVerificationCode = findViewById(R.id.editTextVerificationCode)
        buttonSendCode = findViewById(R.id.buttonSendCode)
        buttonVerifyCode = findViewById(R.id.buttonVerifyCode)
        buttonFindId = findViewById(R.id.buttonFindId)
        buttonResetPassword = findViewById(R.id.buttonResetPassword)

        firestore = FirebaseFirestore.getInstance()

        buttonSendCode.setOnClickListener {
            val phoneNumber = editTextPhone.text.toString()
            sendVerificationCode(formatPhoneNumber(phoneNumber))
        }

        buttonVerifyCode.setOnClickListener {
            val code = editTextVerificationCode.text.toString()
            verifyCode(code)
        }

        buttonFindId.setOnClickListener {
            findUserId()
        }

        buttonResetPassword.setOnClickListener {
            resetUserPassword()
        }
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        return if (phoneNumber.startsWith("0")) {
            "+82" + phoneNumber.substring(1)
        } else {
            phoneNumber
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Toast.makeText(this@FindActivity, "인증 완료", Toast.LENGTH_SHORT).show()
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@FindActivity, "인증 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@FindActivity.verificationId = verificationId
                    Toast.makeText(this@FindActivity, "인증 코드가 전송되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        verificationId?.let {
            val credential = PhoneAuthProvider.getCredential(it, code)
            signInWithCredential(credential)
        } ?: run {
            Toast.makeText(this, "먼저 인증 코드를 전송하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "인증 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "인증 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun findUserId() {
        val phoneNumber = editTextPhone.text.toString()
        firestore.collection("users")
            .whereEqualTo("phoneNumber", phoneNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "등록된 사용자가 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    val id = documents.first().getString("id")
                    Toast.makeText(this, "아이디: $id", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FindActivity", "아이디 찾기 실패", e)
            }
    }

    private fun resetUserPassword() {
        val phoneNumber = editTextPhone.text.toString()
        firestore.collection("users")
            .whereEqualTo("phoneNumber", phoneNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "등록된 사용자가 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    val documentId = documents.first().id
                    showPasswordResetDialog(documentId)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FindActivity", "비밀번호 찾기 실패", e)
            }
    }
    private fun showPasswordResetDialog(documentId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reset_password, null)
        val editTextNewPassword = dialogView.findViewById<EditText>(R.id.editTextNewPassword)
        val editTextConfirmPassword = dialogView.findViewById<EditText>(R.id.editTextConfirmPassword)

        val dialogBuilder = android.app.AlertDialog.Builder(this)
        dialogBuilder.setView(dialogView)
            .setTitle("비밀번호 재설정")
            .setPositiveButton("확인") { _, _ ->
                val newPassword = editTextNewPassword.text.toString()
                val confirmPassword = editTextConfirmPassword.text.toString()

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Firestore에서 비밀번호 업데이트
                firestore.collection("users").document(documentId)
                    .update("password", newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(this, "비밀번호가 성공적으로 재설정되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "비밀번호 재설정 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("FindActivity", "비밀번호 재설정 실패", e)
                    }
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }


}
