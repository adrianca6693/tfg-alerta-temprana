package com.example.alertatemprana.models

data class Posicion(
    val posId: Int? = null,
    val tripId: Int,
    val lat: Double,
    val lon: Double,
    val timestamp: String? = null
)
