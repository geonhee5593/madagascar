package com.example.madagascar.Mypage

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.Main.MainActivity
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MypageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypageactivity)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // UI 요소 참조
        val nicknameTextView = findViewById<TextView>(R.id.nickname)
        val userIdTextView = findViewById<TextView>(R.id.user_id)

        // 유저 정보 가져오기
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nickname = document.getString("username") ?: "닉네임 없음"
                        val id = document.getString("id") ?: "아이디 없음"
                        nicknameTextView.text = "닉네임: $nickname"
                        userIdTextView.text = "아이디: $id"
                    } else {
                        nicknameTextView.text = "닉네임: 정보 없음"
                        userIdTextView.text = "아이디: 정보 없음"
                    }
                }
                .addOnFailureListener { exception ->
                    nicknameTextView.text = "닉네임: 오류 발생"
                    userIdTextView.text = "아이디: 오류 발생"
                }
        } else {
            nicknameTextView.text = "닉네임: 로그인 필요"
            userIdTextView.text = "아이디: 로그인 필요"
        }

        // 버튼 클릭 이벤트 설정
        /* 공지사항으로 화면 전환 코드 */

        val noticeBtn = findViewById<ImageView>(R.id.btn_notice)

        noticeBtn.setOnClickListener {
            val intent = Intent(this, NoticeActivity::class.java)
            startActivity(intent)
        }

        /* 즐겨찾기로 화면 전환 코드 */

        val favoritesBtn = findViewById<ImageView>(R.id.btn_favorites)

        favoritesBtn.setOnClickListener {
            val intent = Intent(this, FavoritesAdapter::class.java)
            startActivity(intent)
        }

        /* 개인 정보로 화면 전환 코드 */

        val HumanBtn = findViewById<ImageView>(R.id.btn_human)

        HumanBtn.setOnClickListener {
            val intent = Intent(this, Human::class.java)
            startActivity(intent)
        }

        val arrowbtn103 = findViewById<ImageView>(R.id.btn_arrow103)

        arrowbtn103.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
