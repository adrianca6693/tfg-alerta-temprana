package com.example.alertatemprana.models

data class Contacto(
    val contactid: Int? = null,
    val userid: Int,
    val name: String,
    val phonenumber: String,
    val priority: Int
)
