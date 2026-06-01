package com.example.alertatemprana.models

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val pin: String
)
