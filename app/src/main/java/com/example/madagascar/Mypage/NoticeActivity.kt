package com.example.madagascar.Mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.Notice
import com.example.madagascar.NoticeAdapter
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NoticeActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var noticeAdapter: NoticeAdapter
    private val noticeList = mutableListOf<Notice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        firestore = FirebaseFirestore.getInstance()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        noticeAdapter = NoticeAdapter(noticeList)
        recyclerView.adapter = noticeAdapter

        fetchNotices()

        val arrowBtn103 = findViewById<ImageView>(R.id.btn_arrow103)
        arrowBtn103.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchNotices() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId != null) {
            // 먼저 Auth UID가 문서 ID로 사용되는 경우 시도
            firestore.collection("users")
                .document(currentUserId)
                .collection("notices")
                .orderBy("timestamp")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e("NoticeActivity", "공지사항 불러오기 실패", e)
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        noticeList.clear()
                        for (document in snapshots) {
                            val message = document.getString("message") ?: continue
                            val timestamp = document.getTimestamp("timestamp")?.toDate()?.toString() ?: "타임스탬프 없음"
                            noticeList.add(Notice(message, timestamp))
                        }
                        noticeAdapter.notifyDataSetChanged()
                    } else {
                        Log.d("NoticeActivity", "공지사항 데이터가 없습니다.")
                    }
                }
        } else {
            Log.d("NoticeActivity", "로그인된 사용자 ID를 가져올 수 없습니다.")
        }
    }

}
