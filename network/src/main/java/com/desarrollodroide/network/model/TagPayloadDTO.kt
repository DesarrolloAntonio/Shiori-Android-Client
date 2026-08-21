package com.desarrollodroide.network.model

import com.google.gson.annotations.SerializedName

/**
 * Request body for creating and renaming a tag.
 *
 * Deliberately not [TagDTO]: that one carries `nBookmarks` as its primary serialized name, which
 * the server does not accept, and it is shaped for reading rather than writing.
 */
data class TagPayloadDTO(
    @SerializedName("name")
    val name: String,
)
