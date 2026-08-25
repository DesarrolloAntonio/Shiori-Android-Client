package com.desarrollodroide.network.di

import com.desarrollodroide.network.BuildConfig
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
                // BODY prints the full request body, and the login request body is the username
                // and password in the clear. That must never reach logcat on a release build.
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .build()
    } // client

    single {
        Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            // Never used: all 17 endpoints pass an absolute @Url built from the server the user
            // configured. Retrofit demands a base url anyway, so this is a reserved .invalid host
            // that cannot resolve. It used to be google.com, which meant a bug that left the
            // server url empty sent the user's requests to a stranger and read the reply as if it
            // were Shiori's. An unresolvable host fails loudly instead.
            .baseUrl("https://server-url-not-set.invalid/")
            .client(get())
            .build()
    } // retrofit

    single { get<Retrofit>().create(RetrofitNetwork::class.java) } // api service

}