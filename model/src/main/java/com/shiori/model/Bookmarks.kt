package com.shiori.model

data class Bookmarks (
    val error: String,
    var maxPage: Int,
    var page: Int,
    var bookmarks: List<Bookmark>,
) {
    constructor(error: String): this(
        error = error,
        maxPage = 0,
        page = 0,
        bookmarks = emptyList()
    )
}
