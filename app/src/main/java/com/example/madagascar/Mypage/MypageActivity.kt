package com.example.madagascar.Mypage

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.API.Festival
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

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val nicknameTextView = findViewById<TextView>(R.id.nickname)
        val userIdTextView = findViewById<TextView>(R.id.user_id)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        nicknameTextView.text = "닉네임: ${document.getString("username") ?: "정보 없음"}"
                        userIdTextView.text = "아이디: ${document.getString("id") ?: "정보 없음"}"
                    }
                }
                .addOnFailureListener {
                    nicknameTextView.text = "닉네임: 오류 발생"
                    userIdTextView.text = "아이디: 오류 발생"
                }
        } else {
            nicknameTextView.text = "닉네임: 로그인 필요"
            userIdTextView.text = "아이디: 로그인 필요"
        }


    /* 즐겨찾기로 화면 전환 코드 */

        val favoritesBtn = findViewById<ImageView>(R.id.btn_favorites)

        favoritesBtn.setOnClickListener {
            val intent = Intent(this, Favorites::class.java)
            startActivity(intent)
        }

        /* 개인 정보로 화면 전환 코드 */

        val HumanBtn = findViewById<ImageView>(R.id.btn_human)

        HumanBtn.setOnClickListener {
            val intent = Intent(this, Human::class.java)
            startActivity(intent)
        }

        val homebtn = findViewById<ImageView>(R.id.homeIcon)

        homebtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val festivalbtn = findViewById<ImageView>(R.id.festivalIcon)

        festivalbtn.setOnClickListener {
            val intent = Intent(this, Festival::class.java)
            startActivity(intent)
        }


        val arrowbtn103 = findViewById<ImageView>(R.id.btn_arrow103)

        arrowbtn103.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
