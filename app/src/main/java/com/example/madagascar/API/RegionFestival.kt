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

class RegionFestival : AppCompatActivity() {
    private lateinit var regionSpinner: Spinner
    private lateinit var searchField: EditText
    private lateinit var searchButton: Button
    private lateinit var regionRecyclerView: RecyclerView
    private val regions = listOf(
        "서울",
        "인천",
        "대전",
        "대구",
        "광주",
        "부산",
        "울산",
        "경기도",
        "강원특별자치도",
        "충청북도",
        "충청남도",
        "경상북도",
        "경상남도",
        "전북특별자치도",
        "전라남도"
    )
    private val festivalsByRegion = mutableMapOf<String, List<FestivalItem>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_region_festival)

        val regionBtn = findViewById<ImageView>(R.id.btn_arrow110)

        regionBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java) // 'FavoritesActivity'로 수정
            startActivity(intent)
        }

        regionSpinner = findViewById(R.id.regionSpinner)
        searchField = findViewById(R.id.searchField)
        searchButton = findViewById(R.id.searchButton)
        regionRecyclerView = findViewById(R.id.regionRecyclerView)

        setupSpinner()
        fetchAllFestivals()

        searchButton.setOnClickListener {
            val query = searchField.text.toString().trim()
            val selectedRegion = regionSpinner.selectedItem.toString()
            if (selectedRegion == "전체") {
                fetchAllFestivals()
            } else {
                fetchRegionFestivals(selectedRegion)
            }
        }
    }


    private fun setupSpinner() {
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("전체") + regions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        regionSpinner.adapter = adapter

        regionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedRegion = regionSpinner.selectedItem.toString()

                if (selectedRegion == "전체") {
                    // 전체 선택 시 기본 화면으로 설정
                    setupRegionRecyclerView()
                } else {
                    val regionFestivals = festivalsByRegion[selectedRegion] ?: emptyList()

                    // 특정 지역 축제만 표시 (가로 2개 배치)
                    updateRecyclerView(
                        mapOf(selectedRegion to regionFestivals),
                        isRegionSpecific = true
                    )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 선택되지 않은 상태일 때 기본 동작 없음
            }
        }
    }

    private fun updateRecyclerView(
        updatedData: Map<String, List<FestivalItem>>,
        isRegionSpecific: Boolean
    ) {
        // 특정 지역 선택 시 가로 2개씩 배치, 아니면 기본 세로 레이아웃 사용
        if (isRegionSpecific) {
            regionRecyclerView.layoutManager = GridLayoutManager(this, 2) // 가로 2개씩 배치
        } else {
            regionRecyclerView.layoutManager = LinearLayoutManager(this) // 세로로 나열
        }

        val adapter = FestivalAdapter(updatedData.values.flatten().toMutableList()) { festival ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("contentId", festival.contentId)
            startActivity(intent)
        }
        regionRecyclerView.adapter = adapter
    }

    private fun fetchAllFestivals() {
        RetrofitClient.instance.getFestivals(page = 1, pageSize = 500)
            .enqueue(object : Callback<FestivalResponse> {
                override fun onResponse(
                    call: Call<FestivalResponse>,
                    response: Response<FestivalResponse>
                ) {
                    val festivals = response.body()?.response?.body?.items?.item ?: emptyList()

                    regions.forEach { region ->
                        festivalsByRegion[region] =
                            festivals.filter { it.addr1?.contains(region) == true }
                    }

                    // 기본 전체 화면은 세로 배치
                    setupRegionRecyclerView()
                }

                override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                    Toast.makeText(this@RegionFestival, "축제 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    // 특정 지역 축제를 가져오는 함수
    private fun fetchRegionFestivals(regionName: String) {
        RetrofitClient.instance.getRegionFestivals(regionName)
            .enqueue(object : Callback<FestivalResponse> {
                override fun onResponse(
                    call: Call<FestivalResponse>,
                    response: Response<FestivalResponse>
                ) {
                    val festivals = response.body()?.response?.body?.items?.item ?: emptyList()

                    festivalsByRegion[regionName] = festivals
                    setupRegionRecyclerView()
                }

                override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                    Toast.makeText(
                        this@RegionFestival,
                        "지역별 축제 데이터를 불러오지 못했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setupRegionRecyclerView() {
        regionRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = RegionListAdapter(regions, festivalsByRegion) { festival ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("contentId", festival.contentId)
            startActivity(intent)
        }
        regionRecyclerView.adapter = adapter
    }


    private fun searchFestivals(query: String, region: String) {
        val filteredFestivals = if (region == "전체") {
            festivalsByRegion.values.flatten().filter { it.title.contains(query, ignoreCase = true) }
        } else {
            festivalsByRegion[region]?.filter { it.title.contains(query, ignoreCase = true) } ?: emptyList()
        }

        val filteredMap = if (region == "전체") festivalsByRegion else mapOf(region to filteredFestivals)
        regionRecyclerView.adapter = RegionListAdapter(listOf(region), filteredMap) { festival ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("contentId", festival.contentId)
            startActivity(intent)
        }
    }
}