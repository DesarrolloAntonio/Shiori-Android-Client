package com.desarrollodroide.network.model

data class LoginResponseMessageDTO (
    val expires: Int?,    // Deprecated, used only for legacy APIs
    val session: String?, // Deprecated, used only for legacy APIs
    val token: String?,
)