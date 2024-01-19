package com.desarrollodroide.network.model

data class LoginResponseDTO (
    val ok: Boolean?,
    val message: LoginResponseMessageDTO?,
    val error: String?
)