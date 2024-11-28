package com.example.madagascar.Main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.API.CommonResponse
import com.example.madagascar.API.DetailActivity
import com.example.madagascar.API.FestivalAdapter
import com.example.madagascar.API.FestivalItem
import com.example.madagascar.API.FestivalResponse
import com.example.madagascar.API.RetrofitClient
import com.example.madagascar.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchResultsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var festivalAdapter: FestivalAdapter
    private val searchResults = mutableListOf<FestivalItem>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        // 뒤로가기 버튼 설정
        val arrowBtnsearch = findViewById<ImageView>(R.id.btn_arrow_search)
        arrowBtnsearch.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.rv_search_results)
        festivalAdapter = FestivalAdapter(searchResults) { festival ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("contentId", festival.contentId)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = festivalAdapter

        val query = intent.getStringExtra("searchQuery")
        if (query != null) {
            fetchSearchResults(query)
        } else {
            Toast.makeText(this, "검색어가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchSearchResults(query: String) {
        RetrofitClient.instance.searchFestivals(keyword = query)
            .enqueue(object : Callback<FestivalResponse> {
                override fun onResponse(
                    call: Call<FestivalResponse>,
                    response: Response<FestivalResponse>
                ) {
                    if (response.isSuccessful) {
                        val festivals = response.body()?.response?.body?.items?.item ?: emptyList()
                        searchResults.clear()
                        festivals.forEach { festival ->
                            // Common API 호출로 기간 정보 추가
                            fetchCommonDetails(festival)
                        }
                    } else {
                        Toast.makeText(
                            this@SearchResultsActivity,
                            "검색 결과를 가져올 수 없습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                    Toast.makeText(
                        this@SearchResultsActivity,
                        "네트워크 오류: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    private fun fetchCommonDetails(festival: FestivalItem) {
        RetrofitClient.instance.getCommon(contentId = festival.contentId)
            .enqueue(object : Callback<CommonResponse> {
                override fun onResponse(
                    call: Call<CommonResponse>,
                    response: Response<CommonResponse>
                ) {
                    if (response.isSuccessful) {
                        val commonItem = response.body()?.response?.body?.items?.item?.firstOrNull()
                        if (commonItem != null) {
                            festival.eventStartDate = commonItem.eventStartDate ?: "미정"
                            festival.eventEndDate = commonItem.eventEndDate ?: "미정"
                        }
                        // 데이터를 업데이트하고 어댑터에 알림
                        searchResults.add(festival)
                        festivalAdapter.notifyDataSetChanged()
                    } else {
                        // 기본 데이터만 추가
                        searchResults.add(festival)
                        festivalAdapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                    // 기본 데이터만 추가
                    searchResults.add(festival)
                    festivalAdapter.notifyDataSetChanged()
                }
            })
    }
}