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
    private val freeBoardCollection = firestore.collection("FreeBoardItems") // Firestore 컬렉션 참조

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var listView: ListView
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button

    private val fullDataList = mutableListOf<FreeBoardItem>() // 전체 게시글 데이터
    private val currentPageList = mutableListOf<FreeBoardItem>() // 현재 페이지 게시글
    private val pageSize = 10 // 한 페이지 게시글 수
    private var currentPage = 0 // 현재 페이지 번호

    private lateinit var adapter: FreeBoardAdapter // 리스트 뷰 어댑터

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
            // 새 글 작성 화면으로 이동
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
    }

    // Firestore에서 게시글 로드
    private fun loadFreeBoardItems() {
        freeBoardCollection.get()
            .addOnSuccessListener { documents ->
                fullDataList.clear()
                for (document in documents) {
                    val item = document.toObject(FreeBoardItem::class.java)
                    item.id = document.id // Firestore 문서 ID를 설정
                    fullDataList.add(item)
                }
                updatePage() // 데이터 로드 후 페이지 업데이트
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 페이지 업데이트
    private fun updatePage() {
        val start = currentPage * pageSize
        val end = min((currentPage + 1) * pageSize, fullDataList.size)
        currentPageList.clear()
        currentPageList.addAll(fullDataList.subList(start, end))
        adapter.notifyDataSetChanged()
    }

    // 검색 결과 필터링
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

    // 새 데이터 추가 및 조회수 업데이트 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> if (resultCode == RESULT_OK) {
                val title = data?.getStringExtra("title") ?: ""
                val content = data?.getStringExtra("content") ?: ""
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis()) // 현재 날짜

                // Firestore에 데이터 추가
                val newItem = FreeBoardItem(title = title, content = content, views = 0, date = currentDate)
                freeBoardCollection.add(newItem) // Firestore에 새 문서 추가
                    .addOnSuccessListener { documentReference ->
                        newItem.id = documentReference.id // Firestore 문서 ID 설정
                        fullDataList.add(0, newItem) // 새 글을 맨 앞에 추가
                        currentPage = 0
                        updatePage()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "데이터 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            2 -> if (resultCode == RESULT_OK) {
                val updatedViews = data?.getIntExtra("updatedViews", 0) ?: 0
                val position = data?.getIntExtra("position", -1) ?: -1
                if (position in fullDataList.indices) {
                    val updatedItem = fullDataList[position]
                    updatedItem.views = updatedViews
                    freeBoardCollection.document(updatedItem.id).set(updatedItem) // Firestore에 조회수 업데이트
                        .addOnSuccessListener {
                            updatePage()
                        }
                        .addOnFailureListener { e -> Toast.makeText(this, "조회수 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}