package com.example.zephyrscaletester.repository

import android.os.Build
import android.util.Log
import com.example.zephyrscaletester.repository.api.CustomInterceptor
import com.example.zephyrscaletester.repository.api.ZephyrServiceApi
import com.example.zephyrscaletester.repository.models.data.PostCreateTestCycleBody
import com.example.zephyrscaletester.repository.models.data.PostCreateTestResultBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

/**
 * Repository to handle Zephyr API calls
 * @param jiraProject: Project in JIRA that holds your Zephyr Scale instance.
 * E.g: For the test EXMPL-123 the project would be EXMPL
 * @param authorizationToken JWT Bearer token used to authenticate with Zephyr
 * @param userID: Zephyr JIRA user ID that will be responsible to interact with Jira
 * @param testCycleFolderID: Folder ID to create Test cycles in
 *
 */
class ZephyrRepository(
    private val jiraProject: String,
    private val authorizationToken: String,
    private val userID: String,
    private val testCycleFolderID: Int
) {
    companion object {
        private val LOG_TAG = ZephyrRepository::class.java.simpleName
        // Zephyr base API url
        private val URL = "https://api.zephyrscale.smartbear.com/v2/"
    }

    /**
     * Create a retrofit service with the ZephyrService Interface
     * @return ZephyrServiceApi interface
     */
    private fun buildRequest() : ZephyrServiceApi {
        val client = OkHttpClient.Builder().apply {
            addInterceptor(CustomInterceptor(authorizationToken))
        }.build()
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create(ZephyrServiceApi::class.java)
    }

    /**
     * Create a Test Cycle on Zephyr
     * https://support.smartbear.com/zephyr-scale-cloud/api-docs/#operation/createTestExecution
     * @param title Test Cycle title
     * @param description Test Cycle description
     */
    fun createTestCycle(title: String, description: String) : String {
        var cycleKey = ""
        val zephyrService = buildRequest()
        val body = PostCreateTestCycleBody(description, testCycleFolderID, title, jiraProject)
        val request = zephyrService.createTestCycle(body)

        try {
            val response = request.execute()
            if(response.isSuccessful) {
                cycleKey = response.body()?.key ?: ""
            } else {
                Log.d(LOG_TAG, "Unsuccessful request ${response.errorBody()}")
            }
        } catch (e: IOException) {
            Log.d(LOG_TAG, "Error while executing Request. $e")
        }

        return cycleKey
    }

    /**
     * Create a Test Result on Zephyr
     * https://support.smartbear.com/zephyr-scale-cloud/api-docs/#operation/createTestExecution
     * @param testID Test ID on Zephyr to attach the result
     * @param testCycle Test Cycle on Zephyr to attach the test result
     * @param result Test Result
     * @param comment Comment to add into the Test Result on Zephyr
     * @param elapsedTime how long did it took to run the test script
     * @param script Test Script used to run the test
     */
    fun addTestResult(testID: String, testCycle: String, result: String, comment: String = "",
                      elapsedTime: Long, script: String = ""): Int {
        var resultID = 0

        val deviceModel =  "${Build.DEVICE} - (${Build.MODEL})"
        val deviceOS = "${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})"
        val testComment = "<b>Device:</b> $deviceModel<br/><b>Android SDK:</b> $deviceOS<br/><b>Script/Method:</b> $script<br/>$comment"

        val zephyrService = buildRequest()
        val body = PostCreateTestResultBody(
            userID,
            testComment,
            userID,
            elapsedTime,
            jiraProject,
            result,
            testID,
            testCycle
        )
        val request = zephyrService.addTestResult(body)

        try {
            val response = request.execute()
            if(response.isSuccessful) {
                resultID = response.body()?.id ?: 0
            } else {
                Log.d(LOG_TAG, "Unsuccessful request ${response.errorBody()}")
            }
        } catch (e: IOException) {
            Log.d(LOG_TAG, "Error while executing Request. $e")
        }

        return resultID
    }
}