package com.desarrollodroide.network.model

import com.google.gson.annotations.SerializedName

data class UpdateCachePayloadDTO(
    @SerializedName("create_archive")
    val createArchive : Boolean,
    @SerializedName("create_ebook")
    val createEbook : Boolean,
    val ids: List<Int>,
    @SerializedName("keep_metadata")
    val keepMetadata : Boolean,
    @SerializedName("skip_exist")
    val skipExist: Boolean
)