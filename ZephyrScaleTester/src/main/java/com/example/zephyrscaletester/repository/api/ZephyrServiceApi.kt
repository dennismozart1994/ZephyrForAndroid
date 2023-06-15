package com.example.zephyrscaletester.repository.api

import com.example.zephyrscaletester.repository.models.data.PostCreateTestCycleBody
import com.example.zephyrscaletester.repository.models.data.PostCreateTestCycleResponse
import com.example.zephyrscaletester.repository.models.data.PostCreateTestResultBody
import com.example.zephyrscaletester.repository.models.data.PostCreateTestResultResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ZephyrServiceApi {
    @POST("testcycles")
    fun createTestCycle(@Body request: PostCreateTestCycleBody): Call<PostCreateTestCycleResponse>

    @POST("testexecutions")
    fun addTestResult(@Body request: PostCreateTestResultBody): Call<PostCreateTestResultResponse>
}