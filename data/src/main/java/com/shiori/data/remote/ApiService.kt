package com.shiori.data.remote

import com.shiori.domain.model.Bookmark
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

  @GET("posts")
  suspend fun getBookmarks(
      @Url url:String
  ): Response<List<Bookmark>>

}
