package com.shiori.data.helpers

import com.google.gson.*
import com.shiori.model.Tag
import java.lang.reflect.Type

class TagTypeAdapter : JsonSerializer<Tag> {
    override fun serialize(src: Tag?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        if (src != null) {
            jsonObject.addProperty("name", src.name)
        }
        return jsonObject
    }
}

