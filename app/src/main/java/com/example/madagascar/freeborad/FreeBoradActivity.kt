package com.example.madagascar.freeborad

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.R
import kotlin.math.min
import java.text.SimpleDateFormat
import java.util.Locale

data class FreeBoardItem(
    val title: String,
    val content: String,
    var views: Int,
    val date : String
)

class FreeBoradActivity : AppCompatActivity() {

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
            startActivityForResult(Intent(this, ListtextmadeActivity::class.java), 1)
        }

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

        // 리스트 아이템 클릭
        listView.setOnItemClickListener { _, _, position, _ ->
            val actualPosition = currentPage * pageSize + position
            val selectedItem = fullDataList[actualPosition]

            // ListItemActivity로 이동
            Intent(this, ListItemActivity::class.java).apply {
                putExtra("title", selectedItem.title)
                putExtra("content", selectedItem.content)
                putExtra("views", selectedItem.views)
                putExtra("position", actualPosition)
                startActivityForResult(this, 2)
            }
        }

        // 검색 버튼 클릭
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            filterAndDisplayData(query)
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

                fullDataList.add(0, FreeBoardItem(title, content, 0, currentDate)) // 등록일 추가
                currentPage = 0
                updatePage()
            }
            2 -> if (resultCode == RESULT_OK) {
                val updatedViews = data?.getIntExtra("updatedViews", 0) ?: 0
                val position = data?.getIntExtra("position", -1) ?: -1
                if (position in fullDataList.indices) {
                    fullDataList[position].views = updatedViews
                    updatePage()
                }
            }
        }
    }
}