package com.example.zephyrscaletester.repository.api

import okhttp3.Interceptor
import okhttp3.Response

class CustomInterceptor(private val bearerToken: String): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Bearer $bearerToken")
            .addHeader("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}