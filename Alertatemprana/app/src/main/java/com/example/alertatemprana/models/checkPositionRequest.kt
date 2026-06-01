package com.example.alertatemprana.models

data class checkPositionRequest(
    val tripid: Int,
    val currentLat: Double,
    val currentLon: Double
)
