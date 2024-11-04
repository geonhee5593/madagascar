package com.example.madagascar.happyguy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.madagascar.Main.MainActivity
import com.example.madagascar.R
import java.util.Calendar
class HappguyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happguy)

        val favoritesBtn = findViewById<ImageView>(R.id.btn_arrow1)

        favoritesBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val monthSpinner: Spinner = findViewById(R.id.spinner)
        val searchButton: Button = findViewById(R.id.clickbutton1)
        val searchEditText: EditText = findViewById(R.id.searchEditText)
        val months = resources.getStringArray(R.array.months)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = adapter

        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        monthSpinner.setSelection(currentMonth)
        //기본값을 현재의 달로 설정하는 코드
        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> replaceFragment(JanuaryFragment())
                    1 -> replaceFragment(FebruaryFragment())
                    2 -> replaceFragment(MarchFragment())
                    3 -> replaceFragment(AprilFragment())
                    4 -> replaceFragment(MayFragment())
                    5 -> replaceFragment(JuneFragment())
                    6 -> replaceFragment(JulyFragment())
                    7 -> replaceFragment(AugustFragment())
                    8 -> replaceFragment(SeptemberFragment())
                    9 -> replaceFragment(OctoberFragment())
                    10 -> replaceFragment(NovemberFragment())
                    11 -> replaceFragment(DecemberFragment())
                } //월별 스크롤 Fragment 화면
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 검색 버튼 클릭 시 검색 결과 화면 표시
        searchButton.setOnClickListener {
            val searchText = searchEditText.text.toString()
            val searchFragment = SearchResultFragment().apply {
                arguments = Bundle().apply {
                    putString("search_query", searchText)
                }
            }
            replaceFragment(searchFragment)
        }
    }

    //fragment 트랙잭션 월별화면 바꿔 하는 코드
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.monthContent, fragment)
            .commit()
    }
}


