package com.example.madagascar

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter

// PagerAdapter를 상속받은 어댑터 클래스
class adapter(var context: Context) : PagerAdapter() {

    // 페이지를 생성하는 메소드
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var view: View? = null
        // 레이아웃 인플레이터 생성
        var inflater = LayoutInflater.from(context)
        // pager_adapter 레이아웃을 인플레이트
        view = inflater.inflate(R.layout.pager_adapter, container, false)

        // 이미지뷰와 텍스트뷰 참조
        var imageView = view.findViewById<ImageView>(R.id.imageView)
        var textView: TextView = view.findViewById(R.id.txt)

        // 각 페이지의 색상 및 텍스트 설정
        when (position) {
            0 -> {
                imageView.setBackgroundColor(Color.parseColor("#bdbdbd"))
                textView.text = "첫번째"
            }
            1 -> {
                imageView.setBackgroundColor(Color.parseColor("#FF0000"))
                textView.text = "두번째"
            }
            2 -> {
                imageView.setBackgroundColor(Color.parseColor("#1DDB16"))
                textView.text = "세번째"
            }
            3 -> {
                imageView.setBackgroundColor(Color.parseColor("#F361DC"))
                textView.text = "네번째"
            }
        }
        // 생성한 뷰를 컨테이너에 추가
        container.addView(view)
        return view
    }

    // 페이지를 제거하는 메소드
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        // 컨테이너에서 뷰 제거
        container.removeView(`object` as View)
    }

    // 뷰가 객체와 일치하는지 확인하는 메소드
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    // 페이지의 개수를 반환하는 메소드
    override fun getCount(): Int {
        return 4 // 총 4페이지
    }
}
