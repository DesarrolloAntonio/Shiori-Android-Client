package com.shiori.network.model

import com.google.gson.annotations.SerializedName

data class AccountDTO (

    @SerializedName("id")
    val id: Int?,

    @SerializedName("username")
    val userName: String?,

    @SerializedName("owner")
    val owner: Boolean?,

    @SerializedName("password")
    var password: String = ""
    )