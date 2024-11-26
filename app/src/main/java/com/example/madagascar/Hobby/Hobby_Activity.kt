package com.example.madagascar.Hobby

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madagascar.API.CommonResponse
import com.example.madagascar.API.DetailActivity
import com.example.madagascar.API.FestivalAdapter
import com.example.madagascar.API.FestivalItem
import com.example.madagascar.API.FestivalResponse
import com.example.madagascar.API.RetrofitClient
import com.example.madagascar.Main.MainActivity
import com.example.madagascar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class hobby_Activity : AppCompatActivity() {

    private lateinit var festivalAdapter: FestivalAdapter
    private lateinit var rvFestivalList: RecyclerView
    private lateinit var tvSelectedInterests: TextView
    private val selectedInterests = mutableListOf<String>() // 선택된 관심사
    private var currentPage = 1
    private var isLoading = false
    private val allFestivals = mutableListOf<FestivalItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interest)

        tvSelectedInterests = findViewById(R.id.tv_selected_interests)
        rvFestivalList = findViewById(R.id.rv_festival_list)

        val backButton = findViewById<ImageView>(R.id.btn_arrow_back)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        rvFestivalList.layoutManager = GridLayoutManager(this, 2)
        festivalAdapter = FestivalAdapter(allFestivals) { festival ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("contentId", festival.contentId)
            startActivity(intent)
        }
        rvFestivalList.adapter = festivalAdapter

        setupScrollListener()
        loadUserInterests()
    }

    private fun setupScrollListener() {
        rvFestivalList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && lastVisibleItemPosition >= totalItemCount - 2) {
                    currentPage++
                    fetchFestivalsByInterests(selectedInterests)
                }
            }
        })
    }

    private fun loadUserInterests() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(it.uid)
                .collection("interests") // 하위 컬렉션 접근
                .get()
                .addOnSuccessListener { documents ->
                    selectedInterests.clear() // 기존 데이터 초기화
                    for (document in documents) {
                        val interestName = document.getString("interestName") // 하위 컬렉션의 필드
                        if (interestName != null) {
                            selectedInterests.add(interestName)
                        }
                    }

                    // 관심사를 TextView에 표시
                    if (selectedInterests.isNotEmpty()) {
                        tvSelectedInterests.text = selectedInterests.joinToString(" ") { "#$it" }
                    } else {
                        tvSelectedInterests.text = "선택된 관심사가 없습니다."
                    }

                    // 관심사에 맞는 축제 로드
                    fetchFestivalsByInterests(selectedInterests)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "관심사 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchFestivalsByInterests(interests: List<String>) {
        if (isLoading || interests.isEmpty()) return
        isLoading = true

        val requests = interests.map { interest ->
            RetrofitClient.instance.searchFestivals(
                keyword = interest,
                page = currentPage,
                pageSize = 20 // 한 번에 가져올 데이터 수
            )
        }

        val festivals = mutableListOf<FestivalItem>()
        var completedRequests = 0

        for (request in requests) {
            request.enqueue(object : Callback<FestivalResponse> {
                override fun onResponse(call: Call<FestivalResponse>, response: Response<FestivalResponse>) {
                    val fetchedFestivals = response.body()?.response?.body?.items?.item ?: emptyList()
                    festivals.addAll(fetchedFestivals)
                    completedRequests++

                    if (completedRequests == requests.size) {
                        // 모든 요청이 완료된 후 날짜 정보를 보강
                        fetchFestivalDetailsWithDates(festivals)
                    }
                }

                override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                    completedRequests++
                    if (completedRequests == requests.size) {
                        // 모든 요청이 실패한 경우에도 기본 데이터만 정렬
                        displaySortedFestivals(allFestivals)
                        isLoading = false
                    }
                }
            })
        }
    }

    private fun fetchFestivalDetailsWithDates(festivals: List<FestivalItem>) {
        val updatedFestivals = mutableListOf<FestivalItem>()
        var completedRequests = 0

        for (festival in festivals) {
            val contentId = festival.contentId ?: continue

            RetrofitClient.instance.getCommon(contentId)
                .enqueue(object : Callback<CommonResponse> {
                    override fun onResponse(
                        call: Call<CommonResponse>,
                        response: Response<CommonResponse>
                    ) {
                        val commonInfo = response.body()?.response?.body?.items?.item?.firstOrNull()

                        if (commonInfo != null) {
                            festival.eventStartDate = commonInfo.eventStartDate ?: "00000000"
                            festival.eventEndDate = commonInfo.eventEndDate ?: "00000000"
                        }

                        updatedFestivals.add(festival)
                        completedRequests++

                        if (completedRequests == festivals.size) {
                            // 데이터를 정렬 후 업데이트
                            displaySortedFestivals(updatedFestivals)
                            isLoading = false
                        }
                    }

                    override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                        completedRequests++

                        if (completedRequests == festivals.size) {
                            displaySortedFestivals(updatedFestivals)
                            isLoading = false
                        }
                    }
                })
        }
    }


    private fun displaySortedFestivals(festivals: List<FestivalItem>) {
        val sortedFestivals = festivals.sortedByDescending { parseDate(it.eventStartDate) }
        festivalAdapter.setFestivals(sortedFestivals)
    }


    private fun parseDate(date: String?): LocalDate {
        return try {
            if (date.isNullOrEmpty() || date.length != 8) LocalDate.MAX
            else LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"))
        } catch (e: Exception) {
            LocalDate.MAX
        }
    }

}
