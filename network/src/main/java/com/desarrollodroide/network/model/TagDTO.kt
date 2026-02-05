package com.desarrollodroide.network.model

import com.google.gson.annotations.SerializedName

data class TagDTO (
    @SerializedName("id")
    val id: Int?,

    @SerializedName("name")
    val name: String?,

    @SerializedName(value = "bookmark_count", alternate = ["nBookmarks"])
    val nBookmarks: Int?,
    )
