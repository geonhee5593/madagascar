package com.example.madagascar.Mylocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.core.location.LocationManagerCompat.isLocationEnabled
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

        Log.d("fragmentActivity", "onCreate: Activity initialized")

        val locationBtn = findViewById<ImageView>(R.id.btn_arrow103)

        locationBtn.setOnClickListener {
            Log.d("fragmentActivity", "onClick: Location button clicked")
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
        Log.d("fragmentActivity", "requestLocationPermission: Checking location permissions")
        // 위치 권한이 허용되지 않았는지 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("fragmentActivity", "requestLocationPermission: Permission not granted")
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Log.d("fragmentActivity", "requestLocationPermission: Showing rationale for permission")
                // 사용자가 이전에 권한을 거부한 경우 안내 메시지 표시
                Toast.makeText(this, "위치 권한이 필요합니다. 설정에서 활성화해주세요.", Toast.LENGTH_SHORT).show()
            }

            // 권한 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            Log.d("fragmentActivity", "requestLocationPermission: Permission already granted")
            // 권한이 이미 허용된 경우 현재 위치 가져오기
            getCurrentLocation()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("fragmentActivity", "onRequestPermissionsResult: Permission granted")
                // 위치 권한이 허용되었을 때 현재 위치 가져오기
                getCurrentLocation()
            } else {
                Log.d("fragmentActivity", "onRequestPermissionsResult: Permission denied")
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    // 사용자가 "다시 묻지 않음"을 선택한 경우 설정 화면으로 이동 안내
                    Toast.makeText(this, "위치 권한을 허용하려면 설정에서 변경하세요.", Toast.LENGTH_LONG).show()
                    goToAppSettings()
                } else {
                    Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun goToAppSettings() {
        Log.d("fragmentActivity", "goToAppSettings: Redirecting to app settings")
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        Log.d("fragmentActivity", "getCurrentLocation: Attempting to get current location")
        if (!isLocationEnabled()) {
            Toast.makeText(this, "위치 서비스가 비활성화되어 있습니다. 활성화해주세요.", Toast.LENGTH_SHORT).show()
            promptEnableLocation()
            return
        }

        val fusedLocationProviderClient =
            com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    Log.d("fragmentActivity", "getCurrentLocation: Location obtained - Lat: ${location.latitude}, Lng: ${location.longitude}")
                    val currentLocation = LatLng(location.latitude, location.longitude)

                    if (::naverMap.isInitialized) {
                        moveCameraToLocation(currentLocation)

                        // LocationOverlay에 현재 위치 표시
                        val locationOverlay = naverMap.locationOverlay
                        locationOverlay.position = currentLocation
                    } else {
                        Log.e("fragmentActivity", "getCurrentLocation: naverMap is not initialized")
                    }
                    fetchNearbyFestivals(location.latitude, location.longitude)
                } else {
                    Log.d("fragmentActivity", "getCurrentLocation: Location is null")
                    Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("LocationError", "위치를 가져오는 중 오류 발생", e)
                Toast.makeText(this, "위치를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun isLocationEnabled(): Boolean {
        val locationManager =
            getSystemService(LOCATION_SERVICE) as android.location.LocationManager
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    }

    private fun promptEnableLocation() {
        val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }



    override fun onMapReady(naverMap: NaverMap) {
        Log.d("fragmentActivity", "onMapReady: Map is ready")
        try {
            this.naverMap = naverMap
            Log.d("fragmentActivity", "NaverMap is initialized")
            naverMap.locationSource = locationSource
            naverMap.mapType = NaverMap.MapType.Navi

            val locationOverlay = naverMap.locationOverlay
            locationOverlay.isVisible = true
            locationOverlay.circleRadius = 500 // 반경 설정 (단위: 미터)
            locationOverlay.circleOutlineWidth = 3 // 테두리 두께 설정 (선택 사항)

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                getCurrentLocation()
            } else {
                requestLocationPermission()
            }

            naverMap.addOnCameraIdleListener {
                val currentCenter = naverMap.cameraPosition.target
                Log.d("fragmentActivity", "Camera idle at Lat: ${currentCenter.latitude}, Lng: ${currentCenter.longitude}")
                fetchNearbyFestivals(currentCenter.latitude, currentCenter.longitude)
            }
        } catch (e: Exception) {
            Log.e("fragmentActivity", "onMapReady: Error initializing map", e)
        }
    }



    private fun moveCameraToLocation(location: LatLng) {
        if (!::naverMap.isInitialized) {
            Log.e("fragmentActivity", "moveCameraToLocation: naverMap is not initialized")
            return
        }
        Log.d("fragmentActivity", "moveCameraToLocation: Moving camera to Lat: ${location.latitude}, Lng: ${location.longitude}")
        val cameraUpdate = CameraUpdate.scrollTo(location)
        naverMap.moveCamera(cameraUpdate)
    }



    private fun fetchNearbyFestivals(latitude: Double, longitude: Double) {
        Log.d("fragmentActivity", "fetchNearbyFestivals: Fetching festivals near Lat: $latitude, Lng: $longitude")
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


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

}
