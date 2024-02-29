package com.desarrollodroide.network.model

import com.google.gson.annotations.SerializedName

data class AccountDTO(

    @SerializedName("id")
    val id: Int? = -1,

    @SerializedName("username")
    val userName: String? = null,

    @SerializedName("password")
    val password: String? = null,

    @SerializedName("owner")
    val isOwner: Boolean? = null,

    @SerializedName("oldPassword")
    val oldPassword: String? = null,

    @SerializedName("newPassword")
    val newPassword: String? = null,

    @SerializedName("isLegacyApi")
    val isLegacyApi: Boolean? = null,
)