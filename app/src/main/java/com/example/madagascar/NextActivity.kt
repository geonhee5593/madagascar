package com.example.madagascar
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class NextActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chipGroup: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next)

        // 선택된 관심 분야를 받아옴
        val selectedInterests = intent.getStringArrayListExtra("selectedInterests")

        // ChipGroup에 선택된 관심 분야 태그 추가
        chipGroup = findViewById(R.id.chipGroup)
        selectedInterests?.forEach { interest ->
            val chip = Chip(this)
            chip.text = "#$interest"
            chip.isClickable = false
            chipGroup.addView(chip)
        }

        // 리사이클러뷰 설정
        recyclerView = findViewById(R.id.recyclerViewContent)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2열로 그리드 레이아웃 설정

       /* // 예시 콘텐츠 데이터 (선택한 관심사에 따라 다르게 설정 가능)
        val contentList = getContentByInterest(selectedInterests)
        val adapter = ContentAdapter(contentList)
        recyclerView.adapter = adapter*/
    }

   /* // 선택된 관심사에 따라 다른 콘텐츠 반환하는 함수 (여기서는 임시 데이터 제공)
    private fun getContentByInterest(interests: ArrayList<String>?): List<Content> {
        val allContent = listOf(
            Content("음악", R.drawable.music_festival_1),
            Content("음악", R.drawable.music_festival_2),
            Content("영화", R.drawable.movie_festival_1)
            // 추가 콘텐츠 가능
        )
        return allContent.filter { interests?.contains(it.category) == true }
    }*/

}
