package com.example.madagascar

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.Main.MainActivity

@Suppress("DEPRECATION")
class FreeBoradActivity : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var listView: ListView
    private val dataList = mutableListOf<String>()
    private lateinit var arrayAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_freeborad)

        // 레이아웃 컴포넌트 초기화
        searchEditText = findViewById(R.id.searchEditText8)
        searchButton = findViewById(R.id.searchButton8)
        listView = findViewById(R.id.listView8)

        // 어댑터 초기화
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, dataList)
        listView.adapter = arrayAdapter

        // 검색 버튼 클릭 리스너
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isEmpty()) {
                arrayAdapter.clear()
                arrayAdapter.addAll(dataList)
            } else {
                val filteredList = dataList.filter { it.contains(query, ignoreCase = true) }
                arrayAdapter.clear()
                arrayAdapter.addAll(filteredList)
            }
            arrayAdapter.notifyDataSetChanged()
        }

        // 글쓰기 버튼 클릭 시 이동
        val writeButton: Button = findViewById(R.id.button11)
        writeButton.setOnClickListener {
            val intent = Intent(this, listtextmadeActivity::class.java)
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            val title = data?.getStringExtra("title")
            val content = data?.getStringExtra("content")

            if (title != null && content != null) {
                dataList.add("$title\n$content")
                arrayAdapter.notifyDataSetChanged() // 어댑터 갱신
            }
            val FreeBoradBtn = findViewById<ImageView>(R.id.btn_back)

            FreeBoradBtn.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}