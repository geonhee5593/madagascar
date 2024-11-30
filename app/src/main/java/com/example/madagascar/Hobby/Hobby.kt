package com.example.madagascar.Hobby

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.Main.MainActivity
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Hobby : AppCompatActivity() {

    private val selectedInterests = mutableListOf<String>()
    private val interests = listOf(
        "음악", "음식", "예술", "스포츠", "영화",
        "문학", "힐링", "환경", "여행"
    )
    private var isFirstLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Intent로 첫 로그인 여부를 확인
        isFirstLogin = intent.getBooleanExtra("isFirstLogin", false)

        // 첫 로그인이 아닐 경우 MainActivity로 바로 이동 후 종료
        if (!isFirstLogin) {
            startActivity(Intent(this, MainActivity::class.java))
            finish() // 현재 액티비티 종료
            return // 아래 코드를 실행하지 않음
        }

        setContentView(R.layout.hobby_screen)
        setupInterestSelection()

        // 건너뛰기 텍스트 표시
        val skipText = findViewById<TextView>(R.id.tv_skip)
        skipText.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(user.uid).update("isFirstLogin", false)
                    .addOnSuccessListener {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "건너뛰기 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun setupInterestSelection() {
        val sharedPreferences = getSharedPreferences("appPreferences", Context.MODE_PRIVATE)
        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        // Firestore에서 기존 관심 분야를 불러오기
        user?.let {
            db.collection("users").document(it.uid).collection("interests")
                .get()
                .addOnSuccessListener { documents ->
                    selectedInterests.clear()
                    for (document in documents) {
                        val interestName = document.getString("interestName")
                        if (interestName != null) {
                            selectedInterests.add(interestName)
                        }
                    }

                    // 버튼 생성 및 상태 설정
                    interests.forEach { interest ->
                        val button = Button(this).apply {
                            text = interest
                            setBackgroundColor(if (selectedInterests.contains(interest)) Color.DKGRAY else Color.LTGRAY)
                            setOnClickListener {
                                if (selectedInterests.contains(interest)) {
                                    selectedInterests.remove(interest)
                                    setBackgroundColor(Color.LTGRAY)
                                } else {
                                    selectedInterests.add(interest)
                                    setBackgroundColor(Color.DKGRAY)
                                }
                            }
                        }
                        gridLayout.addView(button)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "관심사를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
        }

        // 확인 버튼 클릭 이벤트
        val confirmButton = findViewById<Button>(R.id.btn_confirm)
        confirmButton.setOnClickListener {
            sharedPreferences.edit().putStringSet("selectedInterests", selectedInterests.toSet()).apply()

            user?.let {
                val uid = user.uid
                val userInterestsCollection = db.collection("users").document(uid).collection("interests")

                userInterestsCollection.get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            userInterestsCollection.document(document.id).delete()
                        }

                        // 새 관심 분야 저장
                        val interestsData = selectedInterests.map { interest ->
                            hashMapOf("interestName" to interest)
                        }
                        for (interest in interestsData) {
                            userInterestsCollection.add(interest)
                        }

                        // isFirstLogin 필드를 false로 업데이트
                        db.collection("users").document(uid).update("isFirstLogin", false)
                            .addOnSuccessListener {
                                Toast.makeText(this, "관심사가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "관심사 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "관심사 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // 회원가입 시 isFirstLogin 필드를 true로 설정
    fun setIsFirstLoginTrue(uid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).set(mapOf("isFirstLogin" to true))
            .addOnSuccessListener {
                Toast.makeText(this, "isFirstLogin 필드가 설정되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "isFirstLogin 필드 설정 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
