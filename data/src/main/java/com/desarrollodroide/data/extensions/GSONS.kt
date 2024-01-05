package com.desarrollodroide.data.extensions

import com.desarrollodroide.data.helpers.GSON
import com.google.gson.JsonElement

inline fun <reified T> String.toBean() = GSON.fromJson<T>(this)

inline fun <reified T> JsonElement.toBean() = GSON.fromJson<T>(this)

fun Any.toJson() = GSON.toJson(this)

fun JsonElement.toJson() = GSON.toJson(this)

