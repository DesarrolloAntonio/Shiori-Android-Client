package com.desarrollodroide.network.retrofit

import com.desarrollodroide.network.model.AccountDTO
import com.desarrollodroide.network.model.BookmarkDTO
import com.desarrollodroide.network.model.BookmarkResponseDTO
import com.desarrollodroide.network.model.BookmarksDTO
import com.desarrollodroide.network.model.LivenessResponseDTO
import com.desarrollodroide.network.model.LoginResponseDTO
import com.desarrollodroide.network.model.ReadableContentResponseDTO
import com.desarrollodroide.network.model.SessionDTO
import com.desarrollodroide.network.model.SyncBookmarksResponseDTO
import com.desarrollodroide.network.model.TagDTO
import com.desarrollodroide.network.model.TagsDTO
import retrofit2.Response
import retrofit2.http.*

interface RetrofitNetwork {

    @GET()
    suspend fun getBookmarks(
        @Header("X-Session-Id") xSessionId: String,
        @Url url: String
    ): Response<BookmarksDTO>

    @GET()
    suspend fun getPagingBookmarks(
        @Header("X-Session-Id") xSessionId: String,
        @Url url: String
    ): Response<BookmarksDTO>

    @POST()
    suspend fun sendLogin(
        @Url url: String,
        @Body jsonData: String
    ): Response<SessionDTO>

    @Headers("Content-Type: application/json")
    @POST()
    suspend fun sendLoginV1(
        @Url url: String,
        @Body jsonData: String
    ): Response<LoginResponseDTO>

    @POST()
    suspend fun sendLogout(
        @Url url: String,
        @Header("X-Session-Id") xSessionId: String,
    ): Response<String>

    @HTTP(method = "DELETE", hasBody = true)
    suspend fun deleteBookmarks(
        @Url url: String,
        @Header("X-Session-Id") xSessionId: String,
        @Body bookmarkIds: List<Int>
    ): Response<Unit>

    // Add Bookmark
    @POST
    suspend fun addBookmark(
        @Url url: String,
        @Header("X-Session-Id") xSessionId: String,
        @Body body: String
    ): Response<BookmarkDTO>

    @PUT()
    suspend fun editBookmark(
        @Url url: String,
        @Header("X-Session-Id") xSessionId: String,
        @Body body: String
    ): Response<BookmarkDTO>

    @PUT()
    suspend fun updateBookmarksCache(
        @Url url: String,
        @Header("X-Session-Id") xSessionId: String,
        @Body body: String
    ): Response<List<BookmarkDTO>>

    @PUT()
    suspend fun updateBookmarksCacheV1(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Body body: String
    ): Response<BookmarkResponseDTO>

    // Get tags
    @GET()
    suspend fun getTags(
        @Url url: String,
        @Header("Authorization") authorization: String,
    ): Response<TagsDTO>

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

    // Test system liveness
    @GET()
    suspend fun systemLiveness(
        @Url url: String
    ): Response<LivenessResponseDTO>

    @GET()
    suspend fun getBookmarkReadableContent(
        @Url url: String,
        @Header("Authorization") authorization: String,
    ): Response<ReadableContentResponseDTO>

    @POST()
    suspend fun syncBookmarks(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Body body: String
    ): Response<SyncBookmarksResponseDTO>

    @GET
    suspend fun getBookmark(
        @Url url: String,
        @Header("Authorization") authorization: String,
    ): Response<BookmarkDTO>

}
