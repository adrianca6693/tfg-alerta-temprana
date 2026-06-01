package com.example.alertatemprana.models

data class checkPosResponse(
    val message: String,
    val isStopped: Boolean,
    val isSharpTurn: Boolean
)
