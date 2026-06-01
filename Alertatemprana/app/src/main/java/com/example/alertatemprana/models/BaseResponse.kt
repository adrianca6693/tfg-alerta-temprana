package com.example.alertatemprana.models

data class BaseResponse<T>(
    val message: String,
    val user: Usuario,
    val token: String? = null
)
