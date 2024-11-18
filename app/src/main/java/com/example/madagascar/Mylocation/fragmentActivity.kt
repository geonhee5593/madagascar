package com.example.madagascar.Mylocation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.madagascar.API.FestivalItem
import com.example.madagascar.API.FestivalResponse
import com.example.madagascar.API.RetrofitClient
import com.example.madagascar.Main.MainActivity
import com.example.madagascar.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationSource
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import retrofit2.Call

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
        } else {
            // 권한이 이미 허용된 경우
            setupMapWithLocation()
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        // NaverMap 객체를 지역 변수에 저장
        this.naverMap = naverMap

        // 위치 소스를 NaverMap에 설정
        naverMap.locationSource = locationSource

        // 지도 타입과 테마 설정
        naverMap.mapType = NaverMap.MapType.Navi

        // 권한이 허용되면 위치를 설정
        setupMapWithLocation()

        // 지도 초기 위치 설정
        setupMapWithLocation()

        // 주변 축제 정보를 가져와 지도에 표시
        val currentLocation = locationSource.lastLocation
        if (currentLocation != null) {
            fetchNearbyFestivals(currentLocation.latitude, currentLocation.longitude)
        }
    }

    private fun setupMapWithLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 기본 위치를 부천으로 설정
            val bucheonLocation = LatLng(37.5035, 126.7660)
            val cameraUpdate = CameraUpdate.scrollTo(bucheonLocation)
            naverMap.moveCamera(cameraUpdate)

            // 현재 위치로 카메라 이동
            val currentLocation = locationSource.lastLocation
            if (currentLocation != null) {
                val currentLocationUpdate = CameraUpdate.scrollTo(LatLng(currentLocation.latitude, currentLocation.longitude))
                naverMap.moveCamera(currentLocationUpdate)
            }

            // 현재 위치 오버레이 활성화
            val locationOverlay = naverMap.locationOverlay
            locationOverlay.isVisible = true
        }
    }

    private fun fetchNearbyFestivals(lat: Double, lon: Double) {
        val call = RetrofitClient.instance.getNearbyFestivals(lat, lon, 5000)
        call.enqueue(object : retrofit2.Callback<FestivalResponse> {
            override fun onResponse(call: Call<FestivalResponse>, response: retrofit2.Response<FestivalResponse>) {
                if (response.isSuccessful) {
                    response.body()?.response?.body?.items?.item?.let { festivals ->
                        for (festival in festivals) {
                            addMarker(festival)
                        }
                    }
                } else {
                    Log.e("API Error", "Response failed")
                }
            }

            override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                Log.e("API Error", "Request failed: ${t.message}")
            }
        })
    }

    private fun addMarker(festival: FestivalItem) {
        val marker = Marker()
        marker.position = LatLng(festival.latitude, festival.longitude)
        marker.map = naverMap
        marker.captionText = festival.title

        // 마커 클릭 시 축제 정보 표시
        val infoWindow = InfoWindow()
        marker.setOnClickListener {
            infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(this) {
                override fun getText(infoWindow: InfoWindow): CharSequence {
                    return "${festival.title}\n주소: ${festival.addr1}\n기간: ${festival.eventStartDate} ~ ${festival.eventEndDate}"
                }
            }
            infoWindow.open(marker)
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용되면 현재 위치와 기본 위치 설정
                setupMapWithLocation()
            } else {
                // 권한이 거부되면 앱 종료
                finish()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
