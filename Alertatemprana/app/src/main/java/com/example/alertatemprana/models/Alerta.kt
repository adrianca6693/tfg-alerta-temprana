package com.example.alertatemprana.models

data class Alerta(
    val Alertid: Int? = null,
    val tripId: Int,
    val description: String,
    val type: String,
    val lat: Double,
    val lon: Double,
    val timestamp: String? = null
)
