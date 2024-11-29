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
    var id: String = "",       // Firestore 문서 ID
    var title: String = "",    // 제목
    var content: String = "",  // 내용
    var views: Int = 0,        // 조회수
    var date: String = "",     // 등록일
    var userId: String = ""    // 작성자 ID
)

@Suppress("DEPRECATION")
class FreeBoradActivity : AppCompatActivity() {

    // Firestore와 Firebase Auth 초기화
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val freeBoardCollection = firestore.collection("FreeBoardItems") // 자유게시판 컬렉션 참조

    // UI 요소 변수 선언
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var listView: ListView
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button

    // 데이터 관련 변수 선언
    private val fullDataList = mutableListOf<FreeBoardItem>() // Firestore에서 가져온 모든 데이터
    private val currentPageList = mutableListOf<FreeBoardItem>() // 현재 페이지에 표시할 데이터
    private val pageSize = 10 // 한 페이지에 표시할 아이템 수
    private var currentPage = 0 // 현재 페이지 번호

    private lateinit var adapter: FreeBoardAdapter // ListView 어댑터

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_freeborad)

        // UI 요소 초기화
        searchEditText = findViewById(R.id.searchEditText8)
        searchButton = findViewById(R.id.searchButton8)
        listView = findViewById(R.id.listView8)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)

        // 어댑터 연결
        adapter = FreeBoardAdapter(this, currentPageList)
        listView.adapter = adapter

        // '새 글 작성' 버튼 클릭 이벤트 설정
        findViewById<Button>(R.id.button11).setOnClickListener {
            startActivityForResult(Intent(this, ListtextmadeActivity::class.java), 1)
        }

        // Firestore에서 게시글 데이터 불러오기
        loadFreeBoardItems()

        // 이전 페이지 버튼 클릭 이벤트
        prevButton.setOnClickListener {
            if (currentPage > 0) {
                currentPage-- // 페이지 감소
                updatePage() // 페이지 데이터 업데이트
            }
        }

        // 다음 페이지 버튼 클릭 이벤트
        nextButton.setOnClickListener {
            val maxPages = (fullDataList.size + pageSize - 1) / pageSize // 전체 페이지 수 계산
            if (currentPage < maxPages - 1) {
                currentPage++ // 페이지 증가
                updatePage() // 페이지 데이터 업데이트
            }
        }

        // 검색 버튼 클릭 이벤트
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            filterAndDisplayData(query) // 검색 결과 필터링 및 표시
        }

        // 리스트 아이템 클릭 이벤트 (세부 화면으로 이동)
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = currentPageList[position]

            // ListItemActivity로 데이터 전달
            val intent = Intent(this, ListItemActivity::class.java).apply {
                putExtra("title", item.title)
                putExtra("content", item.content)
                putExtra("views", item.views)
                putExtra("documentId", item.id) // 문서 ID 전달
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
                    fullDataList.add(0, item) // 리스트의 맨 앞에 추가
                }
                updatePage() // Firestore에서 데이터 불러온 후 페이지 갱신
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 현재 페이지 데이터를 ListView에 표시
    private fun updatePage() {
        val start = currentPage * pageSize // 시작 인덱스 계산
        val end = min((currentPage + 1) * pageSize, fullDataList.size) // 끝 인덱스 계산
        currentPageList.clear()
        currentPageList.addAll(fullDataList.subList(start, end)) // 현재 페이지 데이터 설정
        adapter.notifyDataSetChanged() // ListView 갱신
    }

    // 검색 결과 필터링 및 표시
    private fun filterAndDisplayData(query: String) {
        val filteredList = if (query.isEmpty()) {
            fullDataList // 검색어가 없으면 전체 데이터 표시
        } else {
            fullDataList.filter {
                it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
            }
        }
        currentPage = 0 // 첫 페이지로 이동
        currentPageList.clear()
        currentPageList.addAll(filteredList.take(pageSize)) // 첫 페이지 데이터 설정
        adapter.notifyDataSetChanged() // ListView 갱신
    }

    // 다른 액티비티에서 돌아왔을 때 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> if (resultCode == RESULT_OK) {
                val title = data?.getStringExtra("newPostTitle") ?: ""
                val content = data?.getStringExtra("newPostContent") ?: ""
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())

                // 현재 로그인된 사용자의 ID 가져오기
                val currentUser = auth.currentUser
                val userId = currentUser?.uid ?: "알 수 없음" // 로그인하지 않은 경우 "알 수 없음"으로 설정

                // Firestore에 새 글 추가
                val newItem = FreeBoardItem(
                    title = title,
                    content = content,
                    views = 0,
                    date = currentDate,
                    userId = userId // 작성자 ID 추가
                )
                freeBoardCollection.add(newItem)
                    .addOnSuccessListener { documentReference ->
                        newItem.id = documentReference.id // Firestore에서 ID 가져오기
                        fullDataList.add(0, newItem) // 리스트 맨 앞에 추가
                        currentPage = 0 // 페이지를 첫 번째 페이지로 초기화
                        updatePage() // UI 갱신
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "게시글 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            2 -> if (resultCode == RESULT_OK) { // 게시글 조회수 업데이트 결과 처리
                val updatedViews = data?.getIntExtra("updatedViews", 0) ?: 0
                val documentId = data?.getStringExtra("documentId") ?: ""

                // Firestore에서 조회수 업데이트
                val position = fullDataList.indexOfFirst { it.id == documentId }
                if (position != -1) {
                    val updatedItem = fullDataList[position]
                    updatedItem.views = updatedViews
                    freeBoardCollection.document(updatedItem.id).set(updatedItem)
                        .addOnSuccessListener {
                            updatePage() // UI 갱신
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "조회수 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}
