package com.desarrollodroide.network.model

import com.google.gson.annotations.SerializedName

data class LoginRequestPayload(
    val username: String,
    val password: String,
    @SerializedName("remember")
    val rememberMe: Int = 1
)