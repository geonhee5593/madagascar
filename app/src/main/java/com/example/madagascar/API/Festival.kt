package com.example.madagascar.API

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Festival : AppCompatActivity() {
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var festivalAdapter: FestivalAdapter
    private lateinit var festivalRecyclerView: RecyclerView
    private var isLoading = false
    private var currentPage = 1
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_festival)

        val categoryRecyclerView = findViewById<RecyclerView>(R.id.categoryRecyclerView)
        festivalRecyclerView = findViewById(R.id.festivalRecyclerView)

        // 카테고리 설정
        val categories = listOf(
            Category("음식", R.drawable.food),
            Category("음악", R.drawable.music),
            Category("영화", R.drawable.movie),
            Category("예술", R.drawable.art),
            Category("스포츠", R.drawable.sports)
        )

        categoryAdapter = CategoryAdapter(categories) { category ->
            currentPage = 1 // 페이지 초기화
            selectedCategory = category?.name // 선택된 카테고리를 저장
            if (category != null) {
                fetchFestivalsByCategory(category.name)
            } else {
                selectedCategory = null
                fetchAllFestivals()
            }
        }

        categoryRecyclerView.adapter = categoryAdapter
        categoryRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        festivalAdapter = FestivalAdapter(mutableListOf()) { festival ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("contentId", festival.contentId)
            startActivity(intent)
        }
        festivalRecyclerView.adapter = festivalAdapter
        festivalRecyclerView.layoutManager = GridLayoutManager(this, 2)

        // 여기서 무한 스크롤 리스너 추가
        setupRecyclerView()

        // 초기 축제 데이터 로드
        fetchAllFestivals()
    }

    private fun setupRecyclerView() {
        festivalRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && lastVisibleItemPosition >= totalItemCount - 2) {
                    currentPage++
                    loadMoreFestivals()
                }
            }
        })
    }

    // 추가 축제 데이터를 가져오는 함수
    private fun loadMoreFestivals() {
        if (isLoading) return
        isLoading = true

        val call = if (selectedCategory != null) {
            RetrofitClient.instance.searchFestivals(
                keyword = selectedCategory!!,
                page = currentPage,
                pageSize = 10
            )
        } else {
            RetrofitClient.instance.getFestivals(
                page = currentPage,
                pageSize = 10
            )
        }
        call.enqueue(object : Callback<FestivalResponse> {
            override fun onResponse(call: Call<FestivalResponse>, response: Response<FestivalResponse>) {
                val moreFestivals = response.body()?.response?.body?.items?.item ?: emptyList()

                // 중복된 축제 방지
                if (moreFestivals.isNotEmpty()) {
                    festivalAdapter.addFestivals(moreFestivals)
                    currentPage++ // 새로운 페이지로 이동
                }
                isLoading = false
            }

            override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                Toast.makeText(this@Festival, "추가 축제 로드 실패", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        })
    }

    private fun fetchAllFestivals() {
        currentPage = 1
        val call = RetrofitClient.instance.getFestivals(page = currentPage, pageSize = 10)
        call.enqueue(object : Callback<FestivalResponse> {
            override fun onResponse(
                call: Call<FestivalResponse>,
                response: Response<FestivalResponse>
            ) {
                val festivals = response.body()?.response?.body?.items?.item ?: emptyList()
                festivalAdapter.setFestivals(festivals)
            }

            override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                Toast.makeText(this@Festival, "API 호출 실패", Toast.LENGTH_SHORT).show()
                Log.e("Festival", "API 호출 오류: ${t.message}")
            }
        })
    }


    private fun fetchFestivalsByCategory(category: String) {
        currentPage = 1
        festivalAdapter.setFestivals(emptyList()) // 기존 데이터 초기화

        val call = RetrofitClient.instance.searchFestivals(category, page = currentPage, pageSize = 10)
        call.enqueue(object : Callback<FestivalResponse> {
            override fun onResponse(call: Call<FestivalResponse>, response: Response<FestivalResponse>) {
                val festivals = response.body()?.response?.body?.items?.item ?: emptyList()
                festivalAdapter.setFestivals(festivals)
                currentPage++
            }

            override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                Toast.makeText(this@Festival, "카테고리 검색 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }
}