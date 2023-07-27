package com.shiori.data.local.room.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.shiori.model.Tag

class TagsConverter {
    @TypeConverter
    fun fromTagsList(tags: List<Tag>): String {
        val gson = Gson()
        return gson.toJson(tags)
    }

    @TypeConverter
    fun toTagsList(tagsString: String): List<Tag> {
        return try {
            val type = object : TypeToken<List<Tag>>() {}.type
            Gson().fromJson(tagsString, type)
        } catch (e: JsonParseException) {
            emptyList()
        }
    }
}