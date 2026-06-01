package com.example.alertatemprana.models

import com.google.gson.annotations.SerializedName

data class updateContactRequest(
    val contactid: Int,
    val name: String,
    val phonenumber: String
)
