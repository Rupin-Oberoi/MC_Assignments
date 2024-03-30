package com.cse535.assignment2_v3

data class Result(
    val latitude: Double,
    val longitude: Double,
    val daily: Daily
) {
    data class Daily(
        val temperature_2m_max: List<Double>,
        val temperature_2m_min: List<Double>
    )
}