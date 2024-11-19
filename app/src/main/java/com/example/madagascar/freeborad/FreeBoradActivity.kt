package com.example.madagascar.freeborad

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.Main.MainActivity
import com.example.madagascar.R
import kotlin.math.min

class FreeBoradActivity : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var listView: ListView
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button

    private val fullDataList = mutableListOf<String>() // 전체 데이터 저장
    private val currentPageList = mutableListOf<String>() // 현재 페이지에 보여질 데이터
    private val pageSize = 10 // 한 페이지에 표시할 항목 수
    private var currentPage = 0 // 현재 페이지 번호

    private lateinit var arrayAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_freeborad)

        // 레이아웃 컴포넌트 초기화
        searchEditText = findViewById(R.id.searchEditText8)
        searchButton = findViewById(R.id.searchButton8)
        listView = findViewById(R.id.listView8)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)

        // 어댑터 초기화
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, currentPageList)
        listView.adapter = arrayAdapter

        // 페이지네이션 버튼 클릭 리스너 설정
        prevButton.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updatePage()
            }
        }

        nextButton.setOnClickListener {
            val maxPages = (fullDataList.size + pageSize - 1) / pageSize // 총 페이지 수
            if (currentPage < maxPages - 1) {
                currentPage++
                updatePage()
            }
        }

        // 글쓰기 버튼 클릭 리스너
        val writeButton: Button = findViewById(R.id.button11)
        writeButton.setOnClickListener {
            val intent = Intent(this, ListtextmadeActivity::class.java)
            startActivityForResult(intent, 1) // 글쓰기 화면으로 이동
        }

        // 리스트 항목 클릭 리스너 추가
        listView.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = fullDataList[position]
            val title = selectedItem.split("\n")[0] // 제목
            val content = selectedItem.split("\n")[1] // 내용

            // 클릭된 항목의 제목과 내용을 ListItemActivity로 전달
            val intent = Intent(this, ListItemActivity::class.java)
            intent.putExtra("title", title)
            intent.putExtra("content", content)
            startActivity(intent) // ListItemActivity로 이동
        }

        // 검색 버튼 클릭 리스너
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            filterAndDisplayData(query)
        }
    }

    // 페이지네이션 업데이트
    private fun updatePage() {
        val start = currentPage * pageSize
        val end = min((currentPage + 1) * pageSize, fullDataList.size)
        currentPageList.clear()
        currentPageList.addAll(fullDataList.subList(start, end))
        arrayAdapter.notifyDataSetChanged()
    }

    // 검색 결과를 필터링하고 데이터 표시
    private fun filterAndDisplayData(query: String) {
        currentPage = 0 // 검색 시 첫 페이지로 초기화
        if (query.isEmpty()) {
            updatePage() // 검색어가 없으면 원래 데이터 표시
        } else {
            val filteredList = fullDataList.filter { it.contains(query, ignoreCase = true) }
            currentPageList.clear()
            currentPageList.addAll(filteredList)
            arrayAdapter.notifyDataSetChanged()
        }
    }

    // 글쓰기 화면에서 데이터 수신
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            val title = data?.getStringExtra("title")
            val content = data?.getStringExtra("content")

            if (title != null && content != null) {
                fullDataList.add(0, "$title\n$content") // 최신 글이 상단에 오도록 추가
                updatePage() // 새 데이터 추가 후 페이지 갱신
            }
        }
    }
}