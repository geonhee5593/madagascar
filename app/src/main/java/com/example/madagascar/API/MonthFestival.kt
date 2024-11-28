package com.example.madagascar.API

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.madagascar.Main.MainActivity

class MonthFestival : AppCompatActivity() {
    private lateinit var monthSpinner: Spinner
    private lateinit var monthRecyclerView: RecyclerView
    private val months = listOf("1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월")
    private val festivalsByMonth = mutableMapOf<String, List<FestivalItem>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_month_festival)

        val monthBtn = findViewById<ImageView>(R.id.btn_month_arrow)

        monthBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java) // 'FavoritesActivity'로 수정
            startActivity(intent)
        }

        monthSpinner = findViewById(R.id.monthSpinner)
        monthRecyclerView = findViewById(R.id.monthRecyclerView)


        setupSpinner()
        fetchAllFestivals()
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("전체") + months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = adapter

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMonth = monthSpinner.selectedItem.toString()

                if (selectedMonth == "전체") {
                    setupMonthRecyclerView()
                } else {
                    val monthFestivals = festivalsByMonth[selectedMonth] ?: emptyList()
                    updateRecyclerView(mapOf(selectedMonth to monthFestivals), isMonthSpecific = true)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 선택되지 않은 상태일 때 기본 동작 없음
            }
        }
    }

    private fun updateRecyclerView(updatedData: Map<String, List<FestivalItem>>, isMonthSpecific: Boolean) {
        if (isMonthSpecific) {
            monthRecyclerView.layoutManager = GridLayoutManager(this, 2)
        } else {
            monthRecyclerView.layoutManager = LinearLayoutManager(this)
        }

        val adapter = FestivalAdapter(updatedData.values.flatten().toMutableList()) { festival ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("contentId", festival.contentId)
            intent.putExtra("previous_screen", "MonthFestival")
            startActivity(intent)
        }
        monthRecyclerView.adapter = adapter
    }

    private fun fetchAllFestivals() {
        RetrofitClient.instance.getFestivals(page = 1, pageSize = 500)
            .enqueue(object : Callback<FestivalResponse> {
                override fun onResponse(call: Call<FestivalResponse>, response: Response<FestivalResponse>) {
                    val festivals = response.body()?.response?.body?.items?.item ?: emptyList()

                    // 월별로 축제 데이터를 분류
                    months.forEachIndexed { index, month ->
                        val monthKey = String.format("%02d", index + 1) // 01, 02 형식
                        festivalsByMonth[month] = festivals.filter {
                            it.eventStartDate.startsWith(monthKey, 4, true)
                        }
                    }

                    setupMonthRecyclerView()
                }

                override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                    Toast.makeText(this@MonthFestival, "축제 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchMonthFestivals(monthName: String) {
        val monthIndex = months.indexOf(monthName) + 1
        val monthKey = String.format("%02d", monthIndex)

        // 특정 월 축제를 필터링
        val filteredFestivals = festivalsByMonth[monthName] ?: emptyList()
        updateRecyclerView(mapOf(monthName to filteredFestivals), isMonthSpecific = true)
    }

    private fun setupMonthRecyclerView() {
        monthRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = MonthListAdapter(months, festivalsByMonth) { festival ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("contentId", festival.contentId)
            startActivity(intent)
        }
        monthRecyclerView.adapter = adapter
    }
}