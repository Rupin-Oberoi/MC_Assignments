package com.cse535.assignment2_v3
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface RetroInterface {
    @GET("v1/archive")
    fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min",
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Call<Result>
}
