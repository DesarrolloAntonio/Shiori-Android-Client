package com.desarrollodroide.network.di

import com.desarrollodroide.network.retrofit.NetworkLoggerInterceptor
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

fun networkingModule() = module {

    single { NetworkLoggerInterceptor() }

    single {
        OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                val sessionHeader = request.header("X-Session-Id")
                if (sessionHeader != null && sessionHeader.isNotEmpty()) {
                    val newRequest = request.newBuilder()
                        .removeHeader("X-Session-Id")
                        .addHeader("Authorization", "Bearer $sessionHeader")
                        .build()
                    chain.proceed(newRequest)
                } else {
                    chain.proceed(request)
                }
            }
            .addInterceptor(get<NetworkLoggerInterceptor>())
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    } // client

    single {
        Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://google.com") //generic url
            .client(get())
            .build()
    } // retrofit

    single { get<Retrofit>().create(RetrofitNetwork::class.java) } // api service

}