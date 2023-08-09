package com.shiori.network.retrofit

import com.shiori.network.model.AccountDTO
import com.shiori.network.model.BookmarkDTO
import com.shiori.network.model.BookmarksDTO
import com.shiori.network.model.SessionDTO
import com.shiori.network.model.TagDTO
import retrofit2.Response
import retrofit2.http.*

interface RetrofitNetwork {

    @GET()
    suspend fun getBookmarks(
        @Header("X-Session-Id") xSessionId: String,
        @Url url: String
    ): Response<BookmarksDTO>

    @POST()
    suspend fun sendLogin(
        @Url url: String,
        @Body jsonData: String
    ): Response<SessionDTO>

    @POST()
    suspend fun sendLogout(
        @Url url: String,
        @Header("X-Session-Id") xSessionId: String,
    ): Response<String>

    @HTTP(method = "DELETE", path = "/api/bookmarks", hasBody = true)
    suspend fun deleteBookmarks(
        @Header("X-Session-Id") xSessionId: String,
        @Body bookmarkIds: List<Int>
    ): Response<Unit>

    // Add Bookmark
    @POST("/api/bookmarks")
    suspend fun addBookmark(
        @Header("X-Session-Id") xSessionId: String,
        @Body body: String
    ): Response<BookmarkDTO>

    @PUT("/api/bookmarks")
    suspend fun editBookmark(
        @Header("X-Session-Id") xSessionId: String,
        @Body body: String
    ): Response<BookmarkDTO>

    // Get tags
    @GET("/api/tags")
    suspend fun getTags(
        @Header("X-Session-Id") xSessionId: String
    ): Response<List<TagDTO>>

    // Rename tag
    @PUT("/api/tags")
    suspend fun renameTag(
        @Header("X-Session-Id") xSessionId: String,
        @Body tag: TagDTO
    ): Response<TagDTO>

    // List accounts
    @GET("/api/accounts")
    suspend fun listAccounts(
        @Header("X-Session-Id") xSessionId: String
    ): Response<List<AccountDTO>>

    // Create account
    @POST("/api/accounts")
    suspend fun createAccount(
        @Header("X-Session-Id") xSessionId: String,
        @Body account: AccountDTO
    ): Response<AccountDTO>

    // Edit account
    @PUT("/api/accounts")
    suspend fun editAccount(
        @Header("X-Session-Id") xSessionId: String,
        @Body account: AccountDTO
    ): Response<AccountDTO>

    // Delete accounts
    @HTTP(method = "DELETE", path = "/api/accounts", hasBody = true)
    suspend fun deleteAccounts(
        @Header("X-Session-Id") xSessionId: String,
        @Body accountNames: List<String>
    ): Response<Unit>

}
