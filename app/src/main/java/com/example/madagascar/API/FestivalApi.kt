package com.example.madagascar.API

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FestivalApi {
    // 1. 전체 축제 조회 API (카테고리 없이 전체 조회)
    @GET("/api/festivals")
    fun getFestivals(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 500,
        @Query("eventStartDate") startDate: String? = null
    ): Call<FestivalResponse>

    // 2. 공통 정보 조회 API
    @GET("/api/common")
    fun getCommon(@Query("contentId") contentId: String): Call<CommonResponse>

    // 3. 소개 정보 조회 API
    @GET("/api/intro")
    fun getIntro(@Query("contentId") contentId: String): Call<IntroResponse>

    // 4. 키워드 검색 조회 API (카테고리 검색)
    @GET("/api/searchFestivals")
    fun searchFestivals(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 500
    ): Call<FestivalResponse>


    // 5. 위치 기반 관광 정보 조회 API
    @GET("/api/nearbyFestivals")
    fun getNearbyFestivals(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int = 5000
    ): Call<FestivalResponse>

    // 6. 지역별 축제 조회 API (추가)
    @GET("/api/regionFestivals")
    fun getRegionFestivals(
        @Query("regionName") regionName: String,
        @Query("areaCode") areaCode: Int // 지역명을 숫자 코드로 전달
    ): Call<FestivalResponse>

    // 7. 카테고리별 축제 조회 API
    @GET("/api/festivalsByCategory")
    fun getFestivalsByCategory(
        @Query("category") category: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 500,
        @Query("eventStartDate") eventStartDate: String? = null,
        @Query("eventEndDate") eventEndDate: String? = null
    ): Call<FestivalResponse>



    abstract fun getRegionFestivals(regionName: String): Call<FestivalResponse>
}