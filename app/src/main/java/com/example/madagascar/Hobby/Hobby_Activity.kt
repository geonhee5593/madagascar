package com.example.madagascar.Hobby

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

class Hobby_Activity : AppCompatActivity() {

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

        // 버튼 클릭 이벤트 설정
        val selectInterestsButton = findViewById<Button>(R.id.btn_select_interests)
        selectInterestsButton.setOnClickListener {
            // Hobby 액티비티로 이동
            val intent = Intent(this, Hobby::class.java)
            startActivity(intent)
        }

        tvSelectedInterests = findViewById(R.id.tv_selected_interests)
        rvFestivalList = findViewById(R.id.rv_festival_list)

        val backButton = findViewById<ImageView>(R.id.btn_arrow_back)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        rvFestivalList.layoutManager = GridLayoutManager(this, 2)
        festivalAdapter = FestivalAdapter(allFestivals) { festival ->

            fetchFestivalDetailsOnClick(festival)
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
                page = 1, // 첫 페이지만 가져옴
                pageSize = 10 // 필요한 만큼만 가져옴
            )
        }

        // Retrofit 비동기 요청 병합
        val festivals = mutableListOf<FestivalItem>()
        var completedRequests = 0

        for (request in requests) {
            request.enqueue(object : Callback<FestivalResponse> {
                override fun onResponse(call: Call<FestivalResponse>, response: Response<FestivalResponse>) {
                    val fetchedFestivals = response.body()?.response?.body?.items?.item ?: emptyList()
                    festivals.addAll(fetchedFestivals)
                    completedRequests++

                    if (completedRequests == requests.size) {
                        fetchFestivalDetailsWithDates(festivals)
                    }
                }

                override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                    completedRequests++
                    if (completedRequests == requests.size) {
                        displaySortedFestivals(festivals)
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

    private fun fetchFestivalDetailsOnClick(festival: FestivalItem) {
        RetrofitClient.instance.getCommon(festival.contentId)
            .enqueue(object : Callback<CommonResponse> {
                override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                    val commonInfo = response.body()?.response?.body?.items?.item?.firstOrNull()
                    if (commonInfo != null) {
                        festival.eventStartDate = commonInfo.eventStartDate ?: "정보 없음"
                        festival.eventEndDate = commonInfo.eventEndDate ?: "정보 없음"
                    }
                    // 축제 상세 화면으로 이동
                    navigateToDetail(festival)
                }

                override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                    navigateToDetail(festival) // 기본 데이터로 이동
                }
            })
    }

    private fun navigateToDetail(festival: FestivalItem) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("contentId", festival.contentId)
        startActivity(intent)
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
