package com.desarrollodroide.network.model

import com.google.gson.annotations.SerializedName

data class UpdateCachePayloadDTO(
    @SerializedName("createArchive")
    val createArchive : Boolean,
    @SerializedName("createEbook")
    val createEbook : Boolean?,
    val ids: List<Int>,
    @SerializedName("keepMetadata")
    val keepMetadata : Boolean,
)