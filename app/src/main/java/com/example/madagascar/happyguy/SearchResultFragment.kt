package com.example.madagascar.happyguy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.madagascar.R

class SearchResultFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_result, container, false)

        // arguments로 전달받은 검색어를 화면에 표시
        val searchQuery = arguments?.getString("search_query")
        val searchTextView: TextView = view.findViewById(R.id.searchTextView)
        searchTextView.text = "검색 결과: $searchQuery"

        return view
    }
}