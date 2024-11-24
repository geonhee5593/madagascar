package com.example.madagascar.Mylocation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.madagascar.API.DetailActivity
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
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.util.FusedLocationSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.naver.maps.map.overlay.Marker
import java.util.*

class fragmentActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private var currentInfoWindow: InfoWindow? = null
    private var markers: MutableList<Marker> = mutableListOf()
    private var handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var fetchRunnable: Runnable
    private var lastFetchTime: Long = 0 // 마지막 fetch 호출 시간

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_main)

        val locationBtn = findViewById<ImageView>(R.id.btn_arrow103)

        locationBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java) // 'FavoritesActivity'로 수정
            startActivity(intent)
        }

        // 위치 소스 초기화
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // 지도 프래그먼트 초기화
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)

        // 위치 권한 요청
        requestLocationPermission()
    }


    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // 위치 권한이 없을 경우 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.mapType = NaverMap.MapType.Navi

        // 현재 위치 오버레이 설정
        val locationOverlay: LocationOverlay = naverMap.locationOverlay
        locationOverlay.isVisible = true

        // 위치 권한 확인 후 현재 위치로 이동
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationSource.lastLocation?.let { location ->
                val currentLocation = LatLng(location.latitude, location.longitude)
                moveCameraToLocation(currentLocation)
                locationOverlay.position = currentLocation

                // 주변 축제 데이터 가져오기
                fetchNearbyFestivals(location.latitude, location.longitude)
            }
        } else {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }

        // 카메라 이동 멈춤 이벤트
        naverMap.addOnCameraIdleListener {
            val currentCenter = naverMap.cameraPosition.target
            fetchNearbyFestivals(currentCenter.latitude, currentCenter.longitude)
        }
    }


    private fun moveCameraToLocation(location: LatLng) {
        val cameraUpdate = CameraUpdate.scrollTo(location)
        naverMap.moveCamera(cameraUpdate)
    }


    private fun fetchNearbyFestivals(latitude: Double, longitude: Double) {
        Log.d("DEBUG", "Fetching festivals at lat: $latitude, lng: $longitude")
        val radius = 5000 // 반경 5km 내의 축제를 검색

        RetrofitClient.instance.getNearbyFestivals(latitude, longitude, radius)
            .enqueue(object : Callback<FestivalResponse> {
                override fun onResponse(
                    call: Call<FestivalResponse>,
                    response: Response<FestivalResponse>
                ) {
                    if (response.isSuccessful) {
                        val festivals = response.body()?.response?.body?.items?.item ?: emptyList()
                        Log.d("DEBUG", "Fetched ${festivals.size} festivals")
                        if (festivals.isEmpty()) {
                            Toast.makeText(
                                this@fragmentActivity,
                                "주변에 축제가 없습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@fragmentActivity,
                                "${festivals.size}개의 축제를 불러왔습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        // 기존 마커 제거
                        markers.forEach { it.map = null }
                        markers.clear()

                        // 새 마커 추가
                        festivals.forEach { festival ->
                            addFestivalMarker(festival)
                        }
                    } else {
                        Toast.makeText(
                            this@fragmentActivity,
                            "API 응답 실패: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<FestivalResponse>, t: Throwable) {
                    Toast.makeText(
                        this@fragmentActivity,
                        "네트워크 오류: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun addFestivalMarker(festival: FestivalItem) {
        val marker = Marker()
        marker.position = LatLng(festival.latitude, festival.longitude)
        marker.map = naverMap
        marker.captionText = festival.title

        marker.setOnClickListener {
            if (currentInfoWindow?.marker == marker) {
                currentInfoWindow?.close()
                currentInfoWindow = null
            } else {
                showFestivalInfo(festival, marker)
            }
            true
        }

        markers.add(marker)
    }


    private fun showFestivalInfo(festival: FestivalItem, marker: Marker) {
        // 기존 팝업 닫기
        currentInfoWindow?.close()

        // 새로운 팝업 생성
        val infoWindow = InfoWindow()
        infoWindow.adapter = object : InfoWindow.DefaultViewAdapter(this) {
            override fun getContentView(infoWindow: InfoWindow): View {
                val view = layoutInflater.inflate(R.layout.festival_info_popup, null)

                // 축제 데이터 표시
                view.findViewById<TextView>(R.id.festivalTitle).text = festival.title
                view.findViewById<TextView>(R.id.festivalDates).text = "기간: ${festival.eventStartDate} ~ ${festival.eventEndDate}"
                view.findViewById<TextView>(R.id.festivalAddress).text = "주소: ${festival.addr1}"
                view.findViewById<TextView>(R.id.festivalPhone).text = "전화번호: ${festival.tel ?: "정보 없음"}"

                // 팝업 클릭 이벤트: 축제 정보 화면으로 이동
                view.setOnClickListener {
                    val intent = Intent(this@fragmentActivity, DetailActivity::class.java)
                    intent.putExtra("contentId", festival.contentId) // 추가 데이터 전달
                    startActivity(intent)
                }

                return view
            }
        }

        // 마커에 팝업 연결
        infoWindow.open(marker)
        currentInfoWindow = infoWindow
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 허용되었을 때 지도 초기화
                if (::naverMap.isInitialized) {
                    onMapReady(naverMap)
                }
            } else {
                // 위치 권한이 거부되었을 때 메인 화면으로 이동
                Toast.makeText(this, "위치 권한이 필요합니다. 메인 화면으로 돌아갑니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // 현재 액티비티 종료
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
