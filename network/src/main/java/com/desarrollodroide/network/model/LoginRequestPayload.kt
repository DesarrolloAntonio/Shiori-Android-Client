package com.desarrollodroide.network.model

import com.google.gson.annotations.SerializedName

data class LoginRequestPayload(
    val username: String,
    val password: String,
    @SerializedName("remember_me")
    val rememberMe: Boolean = true
)