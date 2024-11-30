package com.example.madagascar

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var isEmailVerified = false // 이메일 인증 여부
    private var temporaryPassword: String? = null // 임시 비밀번호 저장 변수
    private var isUsernameAvailable = false // 사용자 이름 중복 확인 여부
    private var isIdAvailable = false // 아이디 중복 확인 여부

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // "축모아" 텍스트에 그라데이션 적용
        val titleTextView: TextView = findViewById(R.id.app_title)
        val paint = titleTextView.paint
        val width = paint.measureText(titleTextView.text.toString())
        val shader = LinearGradient(
            0f, 0f, width, 0f,
            intArrayOf(
                ContextCompat.getColor(this, R.color.deep_blue),
                ContextCompat.getColor(this, R.color.light_blue)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        titleTextView.paint.shader = shader

        // 인증 코드 전송 버튼
        val sendVerificationButton: Button = findViewById(R.id.buttonSendVerificationCode)
        sendVerificationButton.setOnClickListener {
            sendEmailVerification()
        }

        // 인증 확인 버튼
        val verifyCodeButton: Button = findViewById(R.id.buttonVerifyCode)
        verifyCodeButton.setOnClickListener {
            checkEmailVerification()
        }

        // 사용자 이름 중복 확인 버튼
        val checkUsernameButton: Button = findViewById(R.id.buttonCheckUsername)
        checkUsernameButton.setOnClickListener {
            checkUsernameAvailability()
        }

        // 아이디 중복 확인 버튼
        val checkIdButton: Button = findViewById(R.id.buttonCheckId)
        checkIdButton.setOnClickListener {
            checkIdAvailability()
        }

        // 회원가입 버튼
        val registerButton: Button = findViewById(R.id.buttonRegister)
        registerButton.setOnClickListener {
            validateAndRegisterUser()
        }
    }

    private fun checkUsernameAvailability() {
        val username = findViewById<EditText>(R.id.editTextUsername).text.toString()

        if (username.isBlank()) {
            Toast.makeText(this, "사용자 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users").whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    isUsernameAvailable = true
                    Toast.makeText(this, "사용자 이름을 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    isUsernameAvailable = false
                    Toast.makeText(this, "이미 사용 중인 사용자 이름입니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "사용자 이름 확인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIdAvailability() {
        val id = findViewById<EditText>(R.id.editTextId).text.toString()

        if (id.isBlank()) {
            Toast.makeText(this, "아이디를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users").whereEqualTo("id", id).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    isIdAvailable = true
                    Toast.makeText(this, "아이디를 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    isIdAvailable = false
                    Toast.makeText(this, "이미 사용 중인 아이디입니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "아이디 확인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendEmailVerification() {
        val email = findViewById<EditText>(R.id.editTextEmail).text.toString()

        if (email.isBlank()) {
            Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "올바른 이메일 주소를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 복잡한 임시 비밀번호 생성 및 저장
        temporaryPassword = UUID.randomUUID().toString()

        // Firebase Auth로 임시 계정 생성 및 이메일 인증 링크 전송
        auth.createUserWithEmailAndPassword(email, temporaryPassword!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            Toast.makeText(
                                this,
                                "이메일로 인증 링크가 전송되었습니다. 이메일을 확인해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "인증 이메일 전송 실패: ${verificationTask.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "이메일 인증 전송 실패: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun checkEmailVerification() {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener {
            if (user.isEmailVerified) {
                isEmailVerified = true
                val verifyCodeButton = findViewById<Button>(R.id.buttonVerifyCode)
                verifyCodeButton.text = "인증 완료"
                verifyCodeButton.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                Toast.makeText(this, "이메일 인증이 완료되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "이메일 인증이 완료되지 않았습니다. 이메일을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateAndRegisterUser() {
        val username = findViewById<EditText>(R.id.editTextUsername).text.toString()
        val id = findViewById<EditText>(R.id.editTextId).text.toString()
        val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
        val password = findViewById<EditText>(R.id.editTextPassword).text.toString()
        val confirmPassword = findViewById<EditText>(R.id.editTextConfirmPassword).text.toString()

        if (username.isBlank() || id.isBlank() || email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (username.length < 3) {
            Toast.makeText(this, "사용자 이름은 3자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (id.length < 3) {
            Toast.makeText(this, "아이디는 3자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "올바른 이메일 주소를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "비밀번호와 비밀번호 재확인이 다릅니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isEmailVerified) {
            Toast.makeText(this, "이메일 인증을 완료하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isUsernameAvailable) {
            Toast.makeText(this, "사용자 이름 중복 확인을 완료하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isIdAvailable) {
            Toast.makeText(this, "아이디 중복 확인을 완료하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user != null && temporaryPassword != null) {
            // 기존 사용자 정보 업데이트
            val credential = EmailAuthProvider.getCredential(email, temporaryPassword!!)
            user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.updatePassword(password).addOnCompleteListener { passwordUpdateTask ->
                        if (passwordUpdateTask.isSuccessful) {
                            val userData = hashMapOf(
                                "username" to username,
                                "id" to id,
                                "email" to email
                            )
                            firestore.collection("users").document(user.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "회원가입 성공! 로그인하세요.", Toast.LENGTH_SHORT).show()
                                    navigateToLogin()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "회원가입 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "비밀번호 업데이트 실패: ${passwordUpdateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "재인증 실패: ${reauthTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // 사용자 확인 오류
            if (temporaryPassword == null) {
                Toast.makeText(this, "임시 비밀번호를 찾을 수 없습니다. 다시 시도하세요.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "사용자 인증에 실패했습니다. 다시 로그인하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }
}