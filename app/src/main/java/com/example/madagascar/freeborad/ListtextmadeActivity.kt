package com.example.madagascar.freeborad

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class ListtextmadeActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var saveButton: Button
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listtextmade)

        titleEditText = findViewById(R.id.titleEditText9)
        contentEditText = findViewById(R.id.contentEditText9)
        saveButton = findViewById(R.id.saveButton9)

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val content = contentEditText.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 현재 로그인한 Firebase 사용자의 UID 가져오기
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown"
            val usersCollection = firestore.collection("users") // `users` 컬렉션 참조

            // `users` 컬렉션에서 `id` 필드 가져오기
            usersCollection.document(currentUserId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userIdField = document.getString("id") ?: "Unknown" // Firestore의 `id` 필드 값
                        val username = document.getString("username") ?: "익명" // 사용자 이름 가져오기

                        // 게시글 데이터 생성
                        val newPost = hashMapOf(
                            "title" to title,
                            "content" to content,
                            "views" to 0,
                            "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis()),
                            "userId" to userIdField, // 유저의 `id` 필드 저장
                            "username" to username,
                            "timestamp" to System.currentTimeMillis()
                        )

                        // Firestore에 데이터 추가
                        firestore.collection("notices")
                            .add(newPost)
                            .addOnSuccessListener {
                                Toast.makeText(this, "게시글이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "게시글 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "사용자 정보를 가져오는 데 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

}
