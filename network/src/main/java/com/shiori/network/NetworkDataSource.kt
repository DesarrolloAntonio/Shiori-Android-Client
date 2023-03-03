package com.shiori.network

import com.shiori.network.model.BookmarkDTO

interface NetworkDataSource {

    suspend fun getBookmarks(): List<BookmarkDTO>

}
