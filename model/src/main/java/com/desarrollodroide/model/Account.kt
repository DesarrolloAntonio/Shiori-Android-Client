package com.desarrollodroide.model

class Account(
    val id: Int = -1,
    val userName: String,
    val password: String,
    val owner: Boolean ,
    val serverUrl: String,
) {
    constructor() : this(
        id = -1,
        userName = "",
        password = "",
        owner = false,
        serverUrl = ""
    )
}