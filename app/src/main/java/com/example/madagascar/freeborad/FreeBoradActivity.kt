package com.example.madagascar.freeborad

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.min

data class FreeBoardItem(
    var id: String = "",       // Firestore 문서 ID
    var title: String = "",    // 제목
    var content: String = "",  // 내용
    var views: Int = 0,        // 조회수
    var date: String = "",     // 등록일
    var userId: String = "",   // 작성자 ID
    var username: String = "", // 작성자 이름
    var timestamp: Long = 0L   // 게시글 작성 시간 (Unix Timestamp)
)

@Suppress("DEPRECATION")
class FreeBoradActivity : AppCompatActivity() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var listView: ListView
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button

    private val fullDataList = mutableListOf<FreeBoardItem>()
    private val currentPageList = mutableListOf<FreeBoardItem>()
    private val pageSize = 10
    private var currentPage = 0

    private lateinit var adapter: FreeBoardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_freeborad)

        searchEditText = findViewById(R.id.searchEditText8)
        searchButton = findViewById(R.id.searchButton8)
        listView = findViewById(R.id.listView8)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)

        adapter = FreeBoardAdapter(this, currentPageList)
        listView.adapter = adapter

        findViewById<Button>(R.id.button11).setOnClickListener {
            startActivityForResult(Intent(this, ListtextmadeActivity::class.java), 1)
        }

        loadFreeBoardItems()

        prevButton.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updatePage()
            }
        }

        nextButton.setOnClickListener {
            val maxPages = (fullDataList.size + pageSize - 1) / pageSize
            if (currentPage < maxPages - 1) {
                currentPage++
                updatePage()
            }
        }

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            filterAndDisplayData(query)
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val item = currentPageList[position]
            val intent = Intent(this, ListItemActivity::class.java).apply {
                putExtra("title", item.title)
                putExtra("content", item.content)
                putExtra("views", item.views)
                putExtra("documentId", item.id)
            }
            startActivityForResult(intent, 2)
        }
    }

    private fun loadFreeBoardItems() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userNoticesCollection = firestore.collection("users")
                .document(currentUser.uid)
                .collection("notices")

            userNoticesCollection.get()
                .addOnSuccessListener { documents ->
                    fullDataList.clear()
                    for (document in documents) {
                        val item = document.toObject(FreeBoardItem::class.java)
                        item.id = document.id
                        // 삭제된 게시글 필터링
                        if (item.id.isNotEmpty()) {
                            fullDataList.add(0, item)
                        }
                    }
                    updatePage()
                }
                .addOnFailureListener { e ->
                    Log.e("FreeBoradActivity", "Failed to load notices: ${e.message}")
                    Toast.makeText(this, "데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("FreeBoradActivity", "User is not logged in.")
            Toast.makeText(this, "로그인 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePage() {
        // 페이지 데이터 로드 시 범위 확인
        val start = currentPage * pageSize
        val end = min((currentPage + 1) * pageSize, fullDataList.size)

        // 데이터가 비어 있을 경우 처리
        if (fullDataList.isNotEmpty()) {
            currentPageList.clear()
            currentPageList.addAll(fullDataList.subList(start, end))
            adapter.notifyDataSetChanged()
        } else {
            Log.w("FreeBoradActivity", "No data available to display.")
        }
    }

    private fun filterAndDisplayData(query: String) {
        val filteredList = if (query.isEmpty()) {
            fullDataList
        } else {
            fullDataList.filter {
                it.title.contains(query, ignoreCase = true) || it.content.contains(
                    query,
                    ignoreCase = true
                )
            }
        }
        currentPage = 0
        currentPageList.clear()
        currentPageList.addAll(filteredList.take(pageSize))
        adapter.notifyDataSetChanged()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val title = data?.getStringExtra("newPostTitle") ?: ""
            val content = data?.getStringExtra("newPostContent") ?: ""
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())

            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                val timestamp = System.currentTimeMillis()
                val newItem = FreeBoardItem(
                    title = title,
                    content = content,
                    views = 0,
                    date = currentDate,
                    userId = userId,
                    username = currentUser.displayName ?: "익명",
                    timestamp = timestamp
                )

                firestore.collection("users")
                    .document(userId)
                    .collection("notices")
                    .add(newItem)
                    .addOnSuccessListener { documentReference ->
                        newItem.id = documentReference.id
                        fullDataList.add(0, newItem)
                        currentPage = 0
                        updatePage()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "게시글 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}