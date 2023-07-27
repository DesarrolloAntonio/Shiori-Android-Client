package com.shiori.data.helpers

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.shiori.model.Tag

inline fun <reified T> String.toBean() = GSON.fromJson<T>(this)

inline fun <reified T> JsonElement.toBean() = GSON.fromJson<T>(this)

fun Any.toJson() = GSON.toJson(this)

fun JsonElement.toJson() = GSON.toJson(this)

