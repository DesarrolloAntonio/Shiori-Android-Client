package com.desarrollodroide.model

class Account(
    val id: Int = -1,
    val userName: String,
    val password: String,
    val owner: Boolean ,
    val serverUrl: String,
    val isLegacyApi: Boolean,
) {
    constructor() : this(
        id = -1,
        userName = "",
        password = "",
        owner = false,
        serverUrl = "",
        isLegacyApi = false
    )

    companion object {
        val mock = Account(
            id = 1,
            userName = "user@example.com",
            password = "securePassword123",
            owner = true,
            serverUrl = "https://api.example.com",
            isLegacyApi = false
        )
    }
}