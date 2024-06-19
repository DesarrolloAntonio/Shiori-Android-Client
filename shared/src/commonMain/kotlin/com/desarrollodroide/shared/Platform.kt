package com.desarrollodroide.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform