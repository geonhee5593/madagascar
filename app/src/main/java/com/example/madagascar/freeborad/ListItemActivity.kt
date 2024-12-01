package com.example.madagascar.freeborad

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
import com.google.firebase.firestore.ListenerRegistration

data class Comment(
    val id: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    var userId: String = "",
    val username: String = ""
)

class ListItemActivity : AppCompatActivity() {

    private lateinit var commentEditText: EditText
    private lateinit var commentButton: Button
    private lateinit var backButton: Button
    private lateinit var deleteButton: Button
    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var viewsTextView: TextView
    private lateinit var commentsRecyclerView: RecyclerView

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val comments = mutableListOf<Comment>()
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var documentId: String
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listitem)

        // UI 초기화
        titleTextView = findViewById(R.id.titleTextView)
        contentTextView = findViewById(R.id.contentTextView)
        viewsTextView = findViewById(R.id.viewsTextView)
        commentEditText = findViewById(R.id.commentEditText)
        commentButton = findViewById(R.id.commentButton)
        backButton = findViewById(R.id.backButton)
        deleteButton = findViewById(R.id.deleteButton)
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)

        // 전달받은 문서 ID
        documentId = intent.getStringExtra("documentId") ?: return

        // RecyclerView 설정
        commentAdapter = CommentAdapter(comments, this)
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsRecyclerView.adapter = commentAdapter

        // 데이터 로드
        loadPostDetails()
        loadComments()

        // 댓글 추가 버튼
        commentButton.setOnClickListener {
            addComment()
        }

        // 돌아가기 버튼
        backButton.setOnClickListener {
            finish()
        }

        // 게시글 삭제 버튼
        deleteButton.setOnClickListener {
            deletePost()
        }
    }

    private fun loadPostDetails() {
        firestore.collection("notices")
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val title = document.getString("title") ?: "제목 없음"
                    val content = document.getString("content") ?: "내용 없음"
                    val views = document.getLong("views")?.toInt() ?: 0
                    val userid = document.getString("userId") ?: ""

                    titleTextView.text = title
                    contentTextView.text = content
                    viewsTextView.text = "조회수: $views"

                    val currentUserId = auth.currentUser?.uid ?: ""
                    firestore.collection("users").document(currentUserId).get()
                        .addOnSuccessListener { userDoc ->
                            val loggedInUserId = userDoc.getString("id") ?: "" // 현재 로그인 사용자의 `id`

                            if (loggedInUserId == userid) {
                                deleteButton.visibility = Button.VISIBLE // 삭제 버튼 보이기
                                Log.d("ListItemActivity", "삭제 버튼 표시 - 로그인된 ID: $loggedInUserId")
                            } else {
                                deleteButton.visibility = Button.GONE // 삭제 버튼 숨기기
                                Log.d("ListItemActivity", "삭제 버튼 숨김 - 로그인된 ID: $loggedInUserId")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("ListItemActivity", "현재 사용자 정보를 가져오지 못했습니다: ${e.message}")
                            deleteButton.visibility = Button.GONE
                        }

                    // 조회수 업데이트
                    updateViews(views)
                } else {
                    Toast.makeText(this, "게시글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "게시글 정보를 가져올 수 없습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun updateViews(currentViews: Int) {
        firestore.collection("notices")
            .document(documentId)
            .update("views", currentViews + 1)
            .addOnSuccessListener {
                viewsTextView.text = "조회수: ${currentViews + 1}"
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "조회수 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addComment() {
        val commentText = commentEditText.text.toString().trim()
        if (commentText.isEmpty()) {
            Toast.makeText(this, "댓글을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = auth.currentUser?.uid ?: "Unknown"
        val usersCollection = firestore.collection("users")

        usersCollection.document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userIdField = document.getString("id") ?: "Unknown"
                    val username = document.getString("username") ?: "익명"

                    val newComment = Comment(
                        text = commentText,
                        timestamp = System.currentTimeMillis(),
                        userId = userIdField,
                        username = username
                    )

                    firestore.collection("notices")
                        .document(documentId)
                        .collection("comments")
                        .add(newComment)
                        .addOnSuccessListener {
                            commentEditText.text.clear()
                            Toast.makeText(this, "댓글이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "댓글 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "사용자 정보를 가져오는 데 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadComments() {
        listenerRegistration = firestore.collection("notices")
            .document(documentId)
            .collection("comments")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "댓글 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    comments.clear()
                    for (doc in snapshot.documents) {
                        val comment = doc.toObject(Comment::class.java)
                        if (comment != null) {
                            comments.add(comment)
                        }
                    }
                    commentAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun deletePost() {
        firestore.collection("notices")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "게시글 삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }
}