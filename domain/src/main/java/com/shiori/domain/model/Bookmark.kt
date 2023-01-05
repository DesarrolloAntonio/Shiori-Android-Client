package com.shiori.domain.model

data class Bookmark (
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String)