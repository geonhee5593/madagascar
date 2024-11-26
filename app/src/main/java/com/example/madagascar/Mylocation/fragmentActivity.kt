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
import com.example.madagascar.API.CommonResponse
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
    private var lastRequestLat: Double? = null
    private var lastRequestLng: Double? = null
    private var lastRequestRadius: Int = 3000 // 기본 반경 설정

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
        // 기존 요청과 동일한 좌표 및 반경이면 요청하지 않음
        if (lastRequestLat == latitude && lastRequestLng == longitude) {
            Log.d("DEBUG", "이미 요청된 좌표로 추가 요청하지 않음")
            return
        }

        lastRequestLat = latitude
        lastRequestLng = longitude

        RetrofitClient.instance.getNearbyFestivals(latitude, longitude, lastRequestRadius)
            .enqueue(object : Callback<FestivalResponse> {
                override fun onResponse(
                    call: Call<FestivalResponse>,
                    response: Response<FestivalResponse>
                ) {
                    if (response.isSuccessful) {
                        val festivals = response.body()?.response?.body?.items?.item ?: emptyList()
                        Log.d("DEBUG", "Fetched ${festivals.size} festivals")

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

        // 클릭 이벤트 처리
        marker.setOnClickListener {
            if (currentInfoWindow?.marker == marker) {
                currentInfoWindow?.close()
                currentInfoWindow = null
            } else {
                fetchAndShowFestivalInfo(festival, marker)
            }
            true
        }

        markers.add(marker)
    }

    private fun fetchAndShowFestivalInfo(festival: FestivalItem, marker: Marker) {
        // 공통 정보 조회로 기간 데이터를 보강
        RetrofitClient.instance.getCommon(festival.contentId).enqueue(object : Callback<CommonResponse> {
            override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                val commonInfo = response.body()?.response?.body?.items?.item?.firstOrNull()
                if (commonInfo != null) {
                    festival.eventStartDate = commonInfo.eventStartDate ?: "정보 없음"
                    festival.eventEndDate = commonInfo.eventEndDate ?: "정보 없음"
                }
                showFestivalInfo(festival, marker)
            }

            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                showFestivalInfo(festival, marker) // 기본 데이터로 표시
            }
        })
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
                view.findViewById<TextView>(R.id.festivalDates).text =
                    "기간: ${festival.eventStartDate} ~ ${festival.eventEndDate}"
                view.findViewById<TextView>(R.id.festivalAddress).text = "주소: ${festival.addr1}"
                view.findViewById<TextView>(R.id.festivalPhone).text = "전화번호: ${festival.tel ?: "정보 없음"}"

                // 팝업 클릭 이벤트: 축제 정보 화면으로 이동
                view.setOnClickListener {
                    Log.d("Debug", "Navigating to DetailActivity with contentId: ${festival.contentId}")
                    val intent = Intent(this@fragmentActivity, DetailActivity::class.java)
                    intent.putExtra("contentId", festival.contentId)
                    startActivity(intent)
                }

                return view
            }
        }

        // 팝업 클릭이 제대로 작동하도록 설정
        infoWindow.setOnClickListener {
            Log.d("Debug", "InfoWindow clicked for ${festival.title}")
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("contentId", festival.contentId)
            startActivity(intent)
            true
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
