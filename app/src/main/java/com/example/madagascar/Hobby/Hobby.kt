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
        setContentView(R.layout.hobby_screen)

        val sharedPreferences = getSharedPreferences("interests", Context.MODE_PRIVATE)
        val isFirstLogin = sharedPreferences.getBoolean("isFirstLogin", true)
        if (!isFirstLogin) {
            // 첫 로그인이 아니면 관심 분야 선택 화면을 건너뛰고 바로 `MainActivity`로 이동
            startActivity(Intent(this, MainActivity::class.java))
            finish() // 현재 액티비티 종료
            return
        }

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

                val interestsData = hashMapOf(
                    "userId" to uid,
                    "interests" to selectedInterests
                )

                db.collection("users").document(uid)
                    .set(interestsData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "관심사가 저장되었습니다", Toast.LENGTH_SHORT).show()

                        // 첫 로그인 이후 이 화면을 다시 표시하지 않도록 설정
                        sharedPreferences.edit().putBoolean("isFirstLogin", false).apply()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
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
