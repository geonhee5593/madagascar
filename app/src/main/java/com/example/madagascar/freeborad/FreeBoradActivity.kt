package com.example.madagascar.freeborad

import android.content.Intent
import android.os.Bundle
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
    var id: String = "", // Firestore에서 key로 사용할 ID
    var title: String = "",
    var content: String = "",
    var views: Int = 0,
    var date: String = ""
)

@Suppress("DEPRECATION")
class FreeBoradActivity : AppCompatActivity() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val freeBoardCollection = firestore.collection("FreeBoardItems")

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

        // UI 초기화
        searchEditText = findViewById(R.id.searchEditText8)
        searchButton = findViewById(R.id.searchButton8)
        listView = findViewById(R.id.listView8)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)

        // 어댑터 설정
        adapter = FreeBoardAdapter(this, currentPageList)
        listView.adapter = adapter

        // '새 글 작성' 버튼 클릭
        findViewById<Button>(R.id.button11).setOnClickListener {
            startActivityForResult(Intent(this, ListtextmadeActivity::class.java), 1)
        }

        // 게시글 조회
        loadFreeBoardItems()

        // 이전 페이지 버튼 클릭
        prevButton.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updatePage()
            }
        }

        // 다음 페이지 버튼 클릭
        nextButton.setOnClickListener {
            val maxPages = (fullDataList.size + pageSize - 1) / pageSize
            if (currentPage < maxPages - 1) {
                currentPage++
                updatePage()
            }
        }

        // 검색 버튼 클릭
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            filterAndDisplayData(query)
        }

        // 리스트 아이템 클릭 시 세부 화면으로 이동
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = currentPageList[position]
            val intent = Intent(this, ListItemActivity::class.java).apply {
                putExtra("title", item.title)
                putExtra("content", item.content)
                putExtra("views", item.views)
                putExtra("documentId", item.id) // Firestore 문서 ID 전달
            }
            startActivityForResult(intent, 2)
        }
    }

    private fun loadFreeBoardItems() {
        freeBoardCollection.get()
            .addOnSuccessListener { documents ->
                fullDataList.clear()
                for (document in documents) {
                    val item = document.toObject(FreeBoardItem::class.java)
                    item.id = document.id
                    fullDataList.add(item)
                }
                updatePage()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
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
        when (requestCode) {
            1 -> if (resultCode == RESULT_OK) {
                val title = data?.getStringExtra("newPostTitle") ?: ""
                val content = data?.getStringExtra("newPostContent") ?: ""
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())

                // Firestore 저장 대신 로컬 리스트에만 추가
                val newItem = FreeBoardItem(title = title, content = content, views = 0, date = currentDate)
                fullDataList.add(0, newItem)
                currentPage = 0
                updatePage() // UI 갱신
            }
            2 -> if (resultCode == RESULT_OK) {
                val updatedViews = data?.getIntExtra("updatedViews", 0) ?: 0
                val position = data?.getIntExtra("position", -1) ?: -1
                if (position in fullDataList.indices) {
                    val updatedItem = fullDataList[position]
                    updatedItem.views = updatedViews
                    freeBoardCollection.document(updatedItem.id).set(updatedItem)
                        .addOnSuccessListener {
                            updatePage()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "조회수 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}
