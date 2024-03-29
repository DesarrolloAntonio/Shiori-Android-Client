package com.desarrollodroide.network.model

import com.google.gson.annotations.SerializedName

data class TagDTO (
    @SerializedName("id")
    val id: Int?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("nBookmarks")
    val nBookmarks: Int?,
    )