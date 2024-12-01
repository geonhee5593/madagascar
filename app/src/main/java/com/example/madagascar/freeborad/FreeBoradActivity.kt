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
import com.google.firebase.firestore.Query
import kotlin.math.min

data class FreeBoardItem(
    var id: String = "",
    var title: String = "",
    var content: String = "",
    var views: Int = 0,
    var date: String = "",
    var userId: String = "",
    var username: String = "",
    var timestamp: Long = 0L
)

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
                putExtra("documentId", item.id)
            }
            startActivityForResult(intent, 2)
        }
    }

    private fun loadFreeBoardItems() {
        firestore.collection("notices")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                fullDataList.clear()
                for (document in documents) {
                    val item = document.toObject(FreeBoardItem::class.java).apply {
                        id = document.id
                    }
                    // 추가: users 컬렉션에서 id 필드 가져오기
                    val userId = document.getString("userId") ?: "Unknown"
                    firestore.collection("users").document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            val userFieldId = userDoc.getString("id") ?: "Unknown"
                            item.userId = userFieldId // 작성자 ID를 users 컬렉션의 id 값으로 업데이트
                            adapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            item.userId = "Unknown"
                        }
                    fullDataList.add(item)
                }
                updatePage()
            }
            .addOnFailureListener { e ->
                Log.e("FreeBoradActivity", "게시글 로드 실패: ${e.message}")
                Toast.makeText(this, "게시글 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updatePage() {
        val start = currentPage * pageSize
        val end = min((currentPage + 1) * pageSize, fullDataList.size)
        currentPageList.clear()
        currentPageList.addAll(fullDataList.subList(start, end))
        adapter.notifyDataSetChanged()
    }

    private fun filterAndDisplayData(query: String) {
        val filteredList = if (query.isEmpty()) {
            fullDataList
        } else {
            fullDataList.filter {
                it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
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
            val newPostId = data?.getStringExtra("newPostId") ?: return
            loadFreeBoardItems()
        }
    }
}