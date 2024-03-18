package com.desarrollodroide.data.extensions

import com.google.gson.GsonBuilder
import com.desarrollodroide.data.helpers.TagTypeAdapter
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag

fun Bookmark.toBodyJson() =  GsonBuilder()
    .registerTypeAdapter(Tag::class.java, TagTypeAdapter())
    .create().toJson(this)