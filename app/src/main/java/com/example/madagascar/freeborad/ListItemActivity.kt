package com.example.madagascar.freeborad

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.R
import com.google.firebase.firestore.FirebaseFirestore

data class Comment(
    val text: String = "",
    val timestamp: Long = 0
)

class ListItemActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var viewsTextView: TextView
    private lateinit var commentEditText: EditText
    private lateinit var commentButton: Button
    private lateinit var commentsRecyclerView: RecyclerView

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

        val title = intent.getStringExtra("title") ?: "No Title"
        val content = intent.getStringExtra("content") ?: "No Content"
        views = intent.getIntExtra("views", 0)
        documentId = intent.getStringExtra("documentId") ?: ""

        if (documentId.isEmpty()) {
            Toast.makeText(this, "Invalid document ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        titleTextView.text = title
        contentTextView.text = content
        updateViews()

        // 댓글 RecyclerView 설정
        commentAdapter = CommentAdapter(comments)
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsRecyclerView.adapter = commentAdapter

        loadComments()

        // 댓글 등록 버튼 클릭 처리
        commentButton.setOnClickListener {
            val commentText = commentEditText.text.toString().trim()
            if (commentText.isNotEmpty()) {
                addComment(commentText)
            } else {
                Toast.makeText(this, "댓글을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        // 뒤로가기 버튼 클릭 처리
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    // 조회수 업데이트
    private fun updateViews() {
        views++
        viewsTextView.text = "조회수: $views"

        firestore.collection("FreeBoardItems")
            .document(documentId)
            .update("views", views)
            .addOnSuccessListener {
                Toast.makeText(this, "조회수가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "조회수 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 댓글 불러오기
    private fun loadComments() {
        firestore.collection("FreeBoardItems")
            .document(documentId)
            .collection("comments")
            .orderBy("timestamp") // 최신 댓글이 먼저 나오도록 정렬
            .get()
            .addOnSuccessListener { result ->
                comments.clear()
                for (document in result) {
                    val comment = document.toObject(Comment::class.java)
                    comments.add(comment)
                }
                commentAdapter.notifyDataSetChanged() // RecyclerView 업데이트
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "댓글 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 댓글 추가
    private fun addComment(commentText: String) {
        val comment = Comment(commentText, System.currentTimeMillis())

        firestore.collection("FreeBoardItems")
            .document(documentId)
            .collection("comments")
            .add(comment)
            .addOnSuccessListener {
                commentEditText.text.clear()
                loadComments() // 댓글 등록 후 새로 고침
                Toast.makeText(this, "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "댓글 등록 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}