package com.shiori.data.extensions

import com.google.gson.GsonBuilder
import com.shiori.data.helpers.TagTypeAdapter
import com.shiori.model.Bookmark
import com.shiori.model.Tag

fun Bookmark.toBodyJson() =  GsonBuilder()
    .registerTypeAdapter(Tag::class.java, TagTypeAdapter())
    .create().toJson(this)