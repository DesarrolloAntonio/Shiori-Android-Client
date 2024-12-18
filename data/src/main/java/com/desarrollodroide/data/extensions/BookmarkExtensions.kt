package com.desarrollodroide.data.extensions

import com.google.gson.GsonBuilder
import com.desarrollodroide.data.helpers.TagTypeAdapter
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

/**
 * Converts Bookmark to JSON, omitting the imageURL field when empty
 * to prevent empty image generation processing in the backend.
 *
 * @return String containing the JSON representation of the Bookmark
 */
fun Bookmark.toBodyJson() = GsonBuilder()
    .registerTypeAdapter(Tag::class.java, TagTypeAdapter())
    .setExclusionStrategies(object : ExclusionStrategy {
        override fun shouldSkipField(f: FieldAttributes) =
            f.name == "imageURL" && this@toBodyJson.imageURL.isEmpty()
        override fun shouldSkipClass(clazz: Class<*>?) = false
    })
    .create()
    .toJson(this)