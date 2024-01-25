package com.desarrollodroide.model


data class UpdateCachePayload(
    val createArchive : Boolean,
    val createEbook : Boolean,
    val ids: List<Int>,
    val keepMetadata : Boolean,
    val skipExist: Boolean
)