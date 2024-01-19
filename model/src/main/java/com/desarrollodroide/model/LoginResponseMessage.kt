package com.desarrollodroide.model

data class LoginResponseMessage(
    val expires: Int,
    val session: String,
    val token: String
)