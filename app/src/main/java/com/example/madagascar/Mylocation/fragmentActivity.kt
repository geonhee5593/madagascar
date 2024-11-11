package com.example.madagascar.Mylocation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.madagascar.Main.MainActivity
import com.example.madagascar.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationSource
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource

class fragmentActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_main)

        // 위치 소스 초기화
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

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

        // 위치 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        // NaverMap 객체를 지역 변수에 저장
        this.naverMap = naverMap

        // 위치 소스를 NaverMap에 설정
        naverMap.locationSource = locationSource

        // 현재 위치 오버레이 활성화
        val locationOverlay = naverMap.locationOverlay
        locationOverlay.isVisible = true  // 현재 위치 오버레이 보이기

        // 지도 타입과 테마 설정
        naverMap.mapType = NaverMap.MapType.Navi

        // 권한이 허용되었으면 현재 위치로 카메라 이동
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val currentLocation = locationSource.lastLocation
            if (currentLocation != null) {
                val cameraUpdate = CameraUpdate.scrollTo(LatLng(currentLocation.latitude, currentLocation.longitude))
                naverMap.moveCamera(cameraUpdate)
            }
        }

        // 마커 추가 (임의 위치)
        val marker = Marker()
        marker.position = LatLng(37.576, 126.976)
        marker.map = naverMap
        marker.captionText = "별빛 여행"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)  // 부모 메서드 호출
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)
                naverMap.locationOverlay.isVisible = true // 현재 위치 오버레이 보이기

                // 권한이 허용되면 현재 위치로 카메라 이동
                val currentLocation = locationSource.lastLocation
                if (currentLocation != null) {
                    val cameraUpdate = CameraUpdate.scrollTo(LatLng(currentLocation.latitude, currentLocation.longitude))
                    naverMap.moveCamera(cameraUpdate)
                }
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}