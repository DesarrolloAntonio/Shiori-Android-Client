package com.desarrollodroide.network.model

import com.google.gson.annotations.SerializedName

data class TagDTO (
    @SerializedName("id")
    val id: Int?,

    @SerializedName("name")
    val name: String?,

    @SerializedName(value = "nBookmarks", alternate = ["bookmark_count"])
    val nBookmarks: Int?,
    )