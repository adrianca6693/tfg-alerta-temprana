package com.example.alertatemprana.models

data class Trayecto(
    val tripId: Int? = null,
    val userId: Int,
    val name: String,
    val inilat: Double,
    val inilon: Double,
    val destlat: Double,
    val destlon: Double,
    val rute: String,
    val state: String
)
