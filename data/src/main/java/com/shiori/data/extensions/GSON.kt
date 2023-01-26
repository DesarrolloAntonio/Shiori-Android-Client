package com.shiori.data.extensions

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken

object GSON {

    var gson = GsonBuilder().setLenient().create()

    inline fun <reified T> fromJson(json: String): T {
        val type = object : TypeToken<T>() {}.type
        return gson.fromJson(json, type)
    }

    inline fun <reified T> fromJson(jsonElement: JsonElement): T {
        val type = object : TypeToken<T>() {}.type
        return gson.fromJson(jsonElement, type)
    }

    fun toJson(any: Any) = gson.toJson(any)

    fun toJson(jsonElement: JsonElement) = gson.toJson(jsonElement)

}
