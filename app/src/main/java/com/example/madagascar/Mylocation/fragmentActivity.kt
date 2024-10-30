package com.example.madagascar.Mylocation

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.Main.MainActivity
import com.example.madagascar.R
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.geometry.LatLng

class fragmentActivity : AppCompatActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_main)

        // 지도 프래그먼트 초기화
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)

        val arrowbtn103 = findViewById<ImageView>(R.id.btn_arrow103)

        arrowbtn103.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        // 지도 타입과 테마 설정
        naverMap.mapType = NaverMap.MapType.Navi  // 다른 타입으로 실험해보세요

        // 마커 추가
        val marker = Marker()
        marker.position = LatLng(37.576, 126.976)  // 원하는 좌표 설정
        marker.map = naverMap
        marker.captionText = "별빛 여행"  // 첫 번째 이미지처럼 캡션 추가
    }

}