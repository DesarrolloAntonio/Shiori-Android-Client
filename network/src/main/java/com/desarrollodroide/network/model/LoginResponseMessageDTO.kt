package com.desarrollodroide.network.model

import com.google.gson.annotations.SerializedName

data class LoginResponseMessageDTO (
    val expires: Int?, // Deprecated, used only for legacy APIs

    // Map token to session
    @SerializedName(value = "session", alternate = ["token"])
    val session: String?, // Deprecated, used only for legacy APIs

    @SerializedName("token")
    val token: String? = session       
)
