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
            Category("예술", R.drawable.art)
        )

        categoryAdapter = CategoryAdapter(categories) { category ->
            fetchFestivalsByCategory(category.name)
        }

        categoryRecyclerView.adapter = categoryAdapter
        categoryRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        festivalAdapter = FestivalAdapter(emptyList()) { festival ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("contentId", festival.contentId)
            startActivity(intent)
        }
        festivalRecyclerView.adapter = festivalAdapter
        festivalRecyclerView.layoutManager = GridLayoutManager(this, 2)

        fetchAllFestivals()
    }

    private fun fetchAllFestivals() {
        val call = RetrofitClient.instance.getFestivals()
        call.enqueue(object : Callback<FestivalResponse> {
            override fun onResponse(call: Call<FestivalResponse>, response: Response<FestivalResponse>) {
                val festivals = response.body()?.response?.body?.items?.item ?: emptyList()
                festivalAdapter.setFestivals(festivals)
            }

            override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                Toast.makeText(this@Festival, "API 호출 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchFestivalsByCategory(category: String) {
        val call = RetrofitClient.instance.searchFestivals(category)
        call.enqueue(object : Callback<FestivalResponse> {
            override fun onResponse(call: Call<FestivalResponse>, response: Response<FestivalResponse>) {
                val festivals = response.body()?.response?.body?.items?.item ?: emptyList()
                festivalAdapter.setFestivals(festivals)
            }

            override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                Toast.makeText(this@Festival, "카테고리 검색 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }
}