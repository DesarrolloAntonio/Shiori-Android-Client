package com.desarrollodroide.network.model

import com.google.gson.annotations.SerializedName
//import com.shiori.domain.model.Account

data class SessionDTO (
    @SerializedName("session")
    val session: String?,

    @SerializedName("token")
    val token: String?,

    @SerializedName("account")
    val account: AccountDTO?
)