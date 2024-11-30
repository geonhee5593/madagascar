package com.example.madagascar.freeborad

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class ListtextmadeActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var saveButton: Button
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listtextmade)

        // EditText 초기화
        titleEditText = findViewById(R.id.titleEditText9)
        contentEditText = findViewById(R.id.contentEditText9)
        saveButton = findViewById(R.id.saveButton9)

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val content = contentEditText.text.toString().trim()
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            // Firestore에 데이터 저장
            val newPost = hashMapOf(
                "title" to title,
                "content" to content,
                "timestamp" to currentDate,
                "views" to 0
            )

            // Firestore에 게시글 저장
            firestore.collection("users")
                .document(userId)
                .collection("notices")
                .add(newPost)
                .addOnSuccessListener { documentReference ->
                    // 게시글 저장 후 FreeBoardActivity에 데이터 전달
                    val resultIntent = Intent()
                    resultIntent.putExtra("newPostTitle", title)
                    resultIntent.putExtra("newPostContent", content)
                    setResult(RESULT_OK, resultIntent) // FreeBoardActivity에 데이터 전달

                    finish() // Activity 종료
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "게시글 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}