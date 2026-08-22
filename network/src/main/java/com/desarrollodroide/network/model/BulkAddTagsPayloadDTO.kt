package com.desarrollodroide.network.model

import com.google.gson.annotations.SerializedName

/**
 * Body for PUT /api/v1/bookmarks/bulk/tags.
 *
 * Not `ids`, which is what the shipped web asset sends and what the cache endpoint takes. This
 * route wants `bookmark_ids`, and Go answers an unknown field with
 * `{"error":"bookmark_ids should not be empty"}` rather than a 400 that names the real problem.
 */
data class BulkAddTagsPayloadDTO(
    @SerializedName("bookmark_ids")
    val bookmarkIds: List<Int>,
    @SerializedName("tag_ids")
    val tagIds: List<Int>? = null,
    val tags: List<TagNameDTO>? = null,
)

data class TagNameDTO(
    val name: String,
)
