package com.example.madagascar.Hobby

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.Main.MainActivity
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class hobby : AppCompatActivity() {

    private val selectedInterests = mutableListOf<String>()
    private val interests = listOf(
        "음악", "음식", "예술", "패션", "스포츠", "영화",
        "문학", "드라마", "게임", "문화 체험", "동물", "힐링",
        "환경", "여행", "뷰티"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        user?.let {
            db.collection("users").document(it.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Firestore에 저장된 `isFirstLogin` 확인
                        val isFirstLogin = document.getBoolean("isFirstLogin") ?: true
                        if (!isFirstLogin) {
                            // 첫 로그인이 아니므로 `hobby_Activity`로 이동
                            startActivity(Intent(this, MainActivity::class.java))
                            finish() // 현재 액티비티 종료
                            return@addOnSuccessListener
                        } else {
                            // 첫 로그인인 경우에만 관심 분야 선택 화면 표시
                            setContentView(R.layout.hobby_screen)
                            setupInterestSelection()
                        }
                    } else {
                        // 사용자 문서가 없으면 기본 값으로 초기화
                        setContentView(R.layout.hobby_screen)
                        setupInterestSelection()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "데이터 로드 실패", Toast.LENGTH_SHORT).show()
                }
        }
    }
        private fun setupInterestSelection() {
            val sharedPreferences = getSharedPreferences("appPreferences", Context.MODE_PRIVATE)
            val gridLayout = findViewById<GridLayout>(R.id.gridLayout)

        // 동적으로 버튼을 생성하고 GridLayout에 추가
        interests.forEach { interest ->
            val button = Button(this).apply {
                text = interest
                setBackgroundColor(Color.LTGRAY) // 초기 상태
                setOnClickListener {
                    if (selectedInterests.contains(interest)) {
                        selectedInterests.remove(interest)
                        setBackgroundColor(Color.LTGRAY) // 선택 해제 시 색상 변경
                    } else {
                        selectedInterests.add(interest)
                        setBackgroundColor(Color.DKGRAY) // 선택 시 색상 변경
                    }
                }
            }
            gridLayout.addView(button)
        }

        // 확인 버튼 클릭 시 선택한 항목과 전체 관심사 리스트를 전달
            val confirmButton = findViewById<Button>(R.id.btn_confirm)
            confirmButton.setOnClickListener {
                sharedPreferences.edit().putStringSet("selectedInterests", selectedInterests.toSet()).apply()

                // Firebase Firestore에 관심 분야 저장
                val user = FirebaseAuth.getInstance().currentUser
                user?.let {
                    val uid = user.uid
                    val db = FirebaseFirestore.getInstance()

                    val interestsData = selectedInterests.map { interest ->
                        hashMapOf(
                            "interestName" to interest // 각 관심사를 저장
                        )
                    }

                    val userInterestsCollection = db.collection("users").document(uid).collection("interests")

                    // 기존 데이터를 지우고 새로 추가 (중복 방지)
                    userInterestsCollection.get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                userInterestsCollection.document(document.id).delete()
                            }

                            // 새 관심 분야 저장
                            for (interest in interestsData) {
                                userInterestsCollection.add(interest)
                            }

                            // `isFirstLogin` 업데이트
                            db.collection("users").document(uid)
                                .update("isFirstLogin", false)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "관심사가 저장되었습니다", Toast.LENGTH_SHORT).show()

                                    // 메인 화면으로 이동
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "기존 관심사 삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }


            // 건너뛰기 클릭 시 메인 화면으로 이동
        val skipText = findViewById<TextView>(R.id.tv_skip)
        skipText.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
