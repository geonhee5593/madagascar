package com.example.madagascar.API

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FestivalApi {
    @GET("api/festivals")
    fun getFestivals(
        @Query("eventStartDate") eventStartDate: String
    ): Call<FestivalResponse>
}