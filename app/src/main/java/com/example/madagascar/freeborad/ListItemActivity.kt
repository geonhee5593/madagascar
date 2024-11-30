package com.example.madagascar.freeborad

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Comment(
    val text: String = "",
    val timestamp: Long = 0,
    val userId: String = "",
    val username: String = ""  // 사용자 이름 추가
)

class ListItemActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var viewsTextView: TextView
    private lateinit var commentEditText: EditText
    private lateinit var commentButton: Button
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var deleteButton: Button  // 삭제 버튼

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var documentId: String = ""
    private var views: Int = 0

    private lateinit var commentAdapter: CommentAdapter
    private val comments = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listitem)

        titleTextView = findViewById(R.id.titleTextView)
        contentTextView = findViewById(R.id.contentTextView)
        viewsTextView = findViewById(R.id.viewsTextView)
        commentEditText = findViewById(R.id.commentEditText)
        commentButton = findViewById(R.id.commentButton)
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
        deleteButton = findViewById(R.id.deleteButton)  // 초기화

        val title = intent.getStringExtra("title") ?: "No Title"
        val content = intent.getStringExtra("content") ?: "No Content"
        documentId = intent.getStringExtra("documentId") ?: ""

        // documentId가 비어있는 경우 오류 메시지를 띄우고 종료
        if (documentId.isEmpty()) {
            Toast.makeText(this, "잘못된 게시글입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        titleTextView.text = title
        contentTextView.text = content
        updateViews()

        commentAdapter = CommentAdapter(comments, this) // 수정된 부분
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsRecyclerView.adapter = commentAdapter

        loadComments()

        commentButton.setOnClickListener {
            val commentText = commentEditText.text.toString().trim()
            if (commentText.isNotEmpty()) {
                addComment(commentText)
            } else {
                Toast.makeText(this, "댓글을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }
        // 삭제 버튼 클릭 시 동작
        deleteButton.setOnClickListener {
            deletePost()
        }
    }

    private fun deletePost() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return // 로그인된 사용자 ID 가져오기
        firestore.collection("users")
            .document(userId)
            .collection("notices")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent().apply {
                    putExtra("deletedDocumentId", documentId)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "게시글 삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateViews() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return // 로그인된 사용자 ID 가져오기
        firestore.collection("users")
            .document(userId)
            .collection("notices")
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val currentViews = document.getLong("views")?.toInt() ?: 0
                    views = currentViews + 1

                    // 조회수 업데이트
                    firestore.collection("users")
                        .document(userId)
                        .collection("notices")
                        .document(documentId)
                        .update("views", views)
                        .addOnSuccessListener {
                            viewsTextView.text = "조회수: $views"
                            // 조회수 업데이트 후 FreeBoradActivity로 결과 전달
                            val resultIntent = Intent().apply {
                                putExtra("updatedViews", views)
                                putExtra("documentId", documentId)
                            }
                            setResult(RESULT_OK, resultIntent)
                        }
                        .addOnFailureListener { e ->
                            Log.e("ListItemActivity", "조회수 업데이트 실패: ${e.message}")
                            Toast.makeText(this, "조회수 업데이트 실패", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "게시글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ListItemActivity", "게시글 가져오기 실패: ${e.message}")
                Toast.makeText(this, "게시글 조회 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addComment(commentText: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("ListItemActivity", "User is not logged in.")
            Toast.makeText(this, "로그인 후 댓글을 작성해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("ListItemActivity", "Adding comment: $commentText")

        firestore.collection("users")
            .document(userId)
            .collection("notices")
            .document(documentId)
            .collection("comments")
            .add(Comment(commentText, System.currentTimeMillis(), userId, "Unknown"))
            .addOnSuccessListener {
                Log.d("ListItemActivity", "Comment added successfully.")
                commentEditText.text.clear() // 댓글 작성 후 입력란을 비운다.
                loadComments() // 댓글 목록을 다시 불러온다.
                Toast.makeText(this, "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("ListItemActivity", "Failed to add comment: ${e.message}")
                Toast.makeText(this, "댓글 등록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadComments() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("ListItemActivity", "User is not logged in.")
            return
        }

        Log.d("ListItemActivity", "Loading comments for document: $documentId")

        firestore.collection("users")
            .document(userId)
            .collection("notices")
            .document(documentId)
            .collection("comments")
            .orderBy("timestamp") // 댓글 시간 순 정렬
            .get()
            .addOnSuccessListener { result ->
                Log.d("ListItemActivity", "Comments loaded successfully.")
                comments.clear()
                for (document in result) {
                    val comment = document.toObject(Comment::class.java)
                    comments.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("ListItemActivity", "Failed to load comments: ${e.message}")
                Toast.makeText(this, "댓글 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}