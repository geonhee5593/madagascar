package com.example.madagascar.API

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Festival : AppCompatActivity() {

    private lateinit var festivalAdapter: FestivalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_festival)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        festivalAdapter = FestivalAdapter()
        recyclerView.adapter = festivalAdapter

        fetchFestivals()
    }

    private fun fetchFestivals() {
        val call = RetrofitClient.instance.getFestivals("202211")
        call.enqueue(object : Callback<FestivalResponse> {
            override fun onResponse(call: Call<FestivalResponse>, response: Response<FestivalResponse>) {
                if (response.isSuccessful) {
                    val festivals = response.body()?.response?.body?.items?.item ?: emptyList()
                    festivalAdapter.setFestivals(festivals)
                }
            }

            override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                Log.e("Festival", "API 호출 실패: ${t.message}")
            }
        })
    }
}