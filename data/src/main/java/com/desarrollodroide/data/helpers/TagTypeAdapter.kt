package com.desarrollodroide.data.helpers

import com.google.gson.*
import com.desarrollodroide.model.Tag
import com.desarrollodroide.network.model.TagDTO
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


class AddTagDTOAdapter : JsonSerializer<TagDTO> {
    override fun serialize(src: TagDTO?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        if (src?.name != null) {
            jsonObject.addProperty("name", src.name)
        }
        return jsonObject
    }
}
