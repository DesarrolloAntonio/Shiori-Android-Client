package com.shiori.model

class Account(
    val id: Int = -1,
    val userName: String = "",
    val password: String = "",
    val owner: Boolean = false,
    val serverUrl: String = ""
)