package com.shiori.network.retrofit

import com.shiori.network.model.BookmarkDTO
import com.shiori.network.model.SessionDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface RetrofitNetwork {

    @GET("posts")
    fun getBookmarks(
        @Url url: String
    ): Response<List<BookmarkDTO>>

    @POST()
    suspend fun sendLogin(
        @Url url: String,
        @Body jsonData: String
    ): Response<SessionDTO>
}

//
///**
// * Retrofit API declaration for NIA Network API
// */
//private interface RetrofitNiaNetworkApi {
//    @GET(value = "topics")
//    suspend fun getTopics(
//        @Query("id") ids: List<String>?,
//    ): NetworkResponse<List<NetworkTopic>>
//
//    @GET(value = "authors")
//    suspend fun getAuthors(
//        @Query("id") ids: List<String>?,
//    ): NetworkResponse<List<NetworkAuthor>>
//
//    @GET(value = "newsresources")
//    suspend fun getNewsResources(
//        @Query("id") ids: List<String>?,
//    ): NetworkResponse<List<NetworkNewsResource>>
//
//    @GET(value = "changelists/topics")
//    suspend fun getTopicChangeList(
//        @Query("after") after: Int?,
//    ): List<NetworkChangeList>
//
//    @GET(value = "changelists/authors")
//    suspend fun getAuthorsChangeList(
//        @Query("after") after: Int?,
//    ): List<NetworkChangeList>
//
//    @GET(value = "changelists/newsresources")
//    suspend fun getNewsResourcesChangeList(
//        @Query("after") after: Int?,
//    ): List<NetworkChangeList>
//}
//
//private const val NiaBaseUrl = BuildConfig.BACKEND_URL
//
///**
// * Wrapper for data provided from the [NiaBaseUrl]
// */
//@Serializable
//private data class NetworkResponse<T>(
//    val data: T
//)
//
///**
// * [Retrofit] backed [NiaNetworkDataSource]
// */
//class RetrofitNiaNetwork(
//    networkJson: Json
//) : NiaNetworkDataSource {
//
//    private val networkApi = Retrofit.Builder()
//        .baseUrl(NiaBaseUrl)
//        .client(
//            OkHttpClient.Builder()
//                .addInterceptor(
//                    // TODO: Decide logging logic
//                    HttpLoggingInterceptor().apply {
//                        setLevel(HttpLoggingInterceptor.Level.BODY)
//                    }
//                )
//                .build()
//        )
//        .addConverterFactory(
//            @OptIn(ExperimentalSerializationApi::class)
//            networkJson.asConverterFactory("application/json".toMediaType())
//        )
//        .build()
//        .create(RetrofitNiaNetworkApi::class.java)
//
//    override suspend fun getTopics(ids: List<String>?): List<NetworkTopic> =
//        networkApi.getTopics(ids = ids).data
//
//    override suspend fun getAuthors(ids: List<String>?): List<NetworkAuthor> =
//        networkApi.getAuthors(ids = ids).data
//
//    override suspend fun getNewsResources(ids: List<String>?): List<NetworkNewsResource> =
//        networkApi.getNewsResources(ids = ids).data
//
//    override suspend fun getTopicChangeList(after: Int?): List<NetworkChangeList> =
//        networkApi.getTopicChangeList(after = after)
//
//    override suspend fun getAuthorChangeList(after: Int?): List<NetworkChangeList> =
//        networkApi.getAuthorsChangeList(after = after)
//
//    override suspend fun getNewsResourceChangeList(after: Int?): List<NetworkChangeList> =
//        networkApi.getNewsResourcesChangeList(after = after)
//}
