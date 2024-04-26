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

    companion object {
        val mock = User(
            session = "session123",
            token = "token456",
            account = Account.mock,
            error = ""
        )

        val errorMock = User(
            error = "Error occurred"
        )
    }
}