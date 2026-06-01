package com.example.alertatemprana.models

data class tripRequest(
    val userid: Int,
    val name: String,
    val inilat: Double,
    val inilon: Double,
    val destlat: Double,
    val destlon: Double,
    val status: String = "ACTIVO"
)
