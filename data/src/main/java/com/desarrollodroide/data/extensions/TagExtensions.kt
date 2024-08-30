package com.desarrollodroide.data.extensions

import com.desarrollodroide.model.Tag

fun List<Tag>.toTagPattern(): String {
    if (isEmpty()) return ""

    val escapedNames = map { tag ->
        "\"name\":\"${tag.name.replace("\"", "\\\"").replace("'", "''")}\""
    }
    return "%${escapedNames.joinToString("%' OR tags LIKE '%")}%"
}