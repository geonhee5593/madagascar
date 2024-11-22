package com.example.madagascar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val registerButton: Button = findViewById(R.id.buttonRegister)
        registerButton.isEnabled = false // 처음에는 회원가입 버튼 비활성화

        val checkUsernameButton: Button = findViewById(R.id.buttonCheckUsername)
        val checkIdButton: Button = findViewById(R.id.buttonCheckId)
        val sendCodeButton: Button = findViewById(R.id.buttonSendCode)
        val verifyCodeButton: Button = findViewById(R.id.buttonVerifyCode)

        checkUsernameButton.setOnClickListener {
            val username = findViewById<EditText>(R.id.editTextUsername).text.toString()
            if (isValidUsername(username)) {
                checkUsernameAvailability(username)
            } else {
                Toast.makeText(this, "사용자 이름은 2글자 이상 한글, 영어, 숫자만 입력 가능합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        checkIdButton.setOnClickListener {
            val id = findViewById<EditText>(R.id.editTextId).text.toString()
            checkIdAvailability(id)
        }

        sendCodeButton.setOnClickListener {
            val phoneNumber = findViewById<EditText>(R.id.editTextPhone).text.toString()
            val formattedPhoneNumber = formatPhoneNumber(phoneNumber)  // 국제 형식으로 변환
            Log.d("RegisterActivity", "Formatted phone number: $formattedPhoneNumber")
            sendVerificationCode(formattedPhoneNumber)
        }

        verifyCodeButton.setOnClickListener {
            val code = findViewById<EditText>(R.id.editTextVerificationCode).text.toString()
            verifyCode(code)
        }

        registerButton.setOnClickListener {
            validateAndRegisterUser()
        }
    }

    private fun isValidUsername(username: String): Boolean {
        // 한글, 영어, 숫자만 허용하는 정규식 (최소 2글자 이상)
        val usernamePattern = "^[가-힣a-zA-Z0-9]{2,}$"
        return username.matches(usernamePattern.toRegex())
    }

    private fun checkUsernameAvailability(username: String) {
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "사용 가능한 이름입니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "이미 사용중인 이름입니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("RegisterActivity", "사용자 이름 확인 오류", e)
            }
    }

    private fun checkIdAvailability(id: String) {
        firestore.collection("users")
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "이미 사용중인 아이디입니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("RegisterActivity", "아이디 확인 오류", e)
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
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Toast.makeText(this@RegisterActivity, "전화번호 인증 성공", Toast.LENGTH_SHORT).show()
                    enableRegistration()
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@RegisterActivity, "인증 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("RegisterActivity", "onVerificationFailed", e)
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@RegisterActivity.verificationId = verificationId
                    Toast.makeText(this@RegisterActivity, "인증 코드가 전송되었습니다.", Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        auth.setLanguageCode("kr")
    }

    private fun verifyCode(code: String) {
        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithCredential(credential)
        } else {
            Toast.makeText(this, "인증 코드를 전송받아야 합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "전화번호 인증 성공", Toast.LENGTH_SHORT).show()
                    enableRegistration()
                } else {
                    Toast.makeText(this, "전화번호 인증 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun enableRegistration() {
        val registerButton: Button = findViewById(R.id.buttonRegister)
        registerButton.isEnabled = true
    }

    private fun validateAndRegisterUser() {
        val username = findViewById<EditText>(R.id.editTextUsername).text.toString()
        val id = findViewById<EditText>(R.id.editTextId).text.toString()
        val password = findViewById<EditText>(R.id.editTextPassword).text.toString()
        val confirmPassword = findViewById<EditText>(R.id.editTextConfirmPassword).text.toString()

        if (password != confirmPassword) {
            Toast.makeText(this, "비밀번호와 비밀번호 재확인이 다릅니다.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users").whereEqualTo("username", username).get()
            .addOnSuccessListener { usernameDocs ->
                if (!usernameDocs.isEmpty) {
                    Toast.makeText(this, "이미 사용중인 이름입니다.", Toast.LENGTH_SHORT).show()
                } else {
                    firestore.collection("users").whereEqualTo("id", id).get()
                        .addOnSuccessListener { idDocs ->
                            if (!idDocs.isEmpty) {
                                Toast.makeText(this, "이미 사용중인 아이디입니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                registerUser()
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("RegisterActivity", "회원가입 조건 확인 오류", e)
            }
    }

    private fun registerUser() {
        val username = findViewById<EditText>(R.id.editTextUsername).text.toString()
        val id = findViewById<EditText>(R.id.editTextId).text.toString()
        val phoneNumber = findViewById<EditText>(R.id.editTextPhone).text.toString()
        val password = findViewById<EditText>(R.id.editTextPassword).text.toString()

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid // Firebase Auth UID
            val userData = hashMapOf(
                "username" to username,
                "id" to id,
                "phoneNumber" to phoneNumber,
                "password" to password,
                "isFirstLogin" to true, // 첫 로그인 플래그 추가
                "isAdmin" to false
            )

            // Firestore에 UID를 문서 ID로 사용
            firestore.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                    Log.d("RegisterActivity", "회원가입 정보 Firestore에 저장됨: $username, $id, $phoneNumber")
                    navigateToLogin()
                }
                .addOnFailureListener { e ->
                    Log.e("RegisterActivity", "회원가입 오류", e)
                    Toast.makeText(this, "회원가입 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "사용자 인증 정보가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun navigateToLogin() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }
}
