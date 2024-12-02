package com.example.madagascar.freeborad

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.Main.MainActivity
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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
    private var listenerRegistration: ListenerRegistration? = null

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
            startActivity(Intent(this, ListtextmadeActivity::class.java))
        }

        // 실시간 데이터 감지 설정
        listenToFreeBoardItems()

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
            startActivity(intent)
        }
        val imageViewToMain: ImageView = findViewById(R.id.btn_back)
        imageViewToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun listenToFreeBoardItems() {
        listenerRegistration = firestore.collection("notices")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("FreeBoradActivity", "실시간 데이터 로드 실패: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    fullDataList.clear()
                    for (document in snapshots.documents) {
                        val item = document.toObject(FreeBoardItem::class.java)?.apply {
                            id = document.id
                        }
                        if (item != null) {
                            fullDataList.add(item)
                        }
                    }
                    updatePage()
                }
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

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove() // Listener 해제
    }
}