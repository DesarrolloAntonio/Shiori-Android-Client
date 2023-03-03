package com.shiori.data.mapper

import com.shiori.data.UserPreferences
import com.shiori.model.Account
import com.shiori.model.Bookmark
import com.shiori.model.Tag
import com.shiori.model.User
import com.shiori.network.model.*

fun SessionDTO.toDomainModel() = User(
    session = session?:"",
    account = account?.toDomainModel()?:com.shiori.model.Account(
        id = -1,
        userName = "",
        password = "",
        owner = false
    )
)

fun AccountDTO.toDomainModel() = com.shiori.model.Account(
    id = id?:-1,
    userName = userName?:"",
    password = password?:"",
    owner = owner?:false
)

fun SessionDTO.toProtoEntityWith(newPassword: String) = UserPreferences.newBuilder()
    .setSession(session?:"")
    .setUsername(account?.userName?:"")
    .setPassword(newPassword)
    .setId(account?.id?:-1)
    .setOwner(account?.owner?:false)
    .build()

fun SessionDTO.toDomainModelWith(newPassword: String) = User(
    session = session?:"",
    account = account?.toDomainModelWith(newPassword)?:Account(
        id = -1,
        userName = "",
        password = "",
        owner = false
    )
)

fun AccountDTO.toDomainModelWith(newPassword: String) = Account(
    id = id?:-1,
    userName = userName?:"",
    password = newPassword,
    owner = false
)

fun BookmarkDTO.toDomainModel() = Bookmark(
    id = id?:0,
    url = url?:"",
    title = title?:"",
    excerpt = excerpt?:"",
    author = author?:"",
    public = public?:0,
    modified = modified?:"",
    imageURL = imageURL?:"",
    hasContent = hasContent?:false,
    hasArchive = hasArchive?:false,
    tags = tags?.map { it.toDomainModel() }?: emptyList(),
    createArchive = createArchive?:false
)

fun TagDTO.toDomainModel() = Tag(
    id = id?:0,
    name = name?:""
)

fun Account.toRequestBody() =
    LoginBodyContent(
        username = userName,
        password = password
    )




