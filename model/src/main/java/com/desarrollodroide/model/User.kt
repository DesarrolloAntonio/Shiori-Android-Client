package com.desarrollodroide.model

data class User(
    val session: String,
    val token: String,
    val account: Account,
    val error: String = ""
) {
    fun hasSession() = session.isNotEmpty()

    constructor(error: String) : this(
        token = "",
        session = "",
        account = Account(),
        error = error
    )
}