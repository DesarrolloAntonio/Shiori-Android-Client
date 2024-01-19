package com.desarrollodroide.data.extensions

fun String.removeTrailingSlash(): String {
    return if (this.endsWith("/")) {
        this.dropLast(1)
    } else {
        this
    }
}