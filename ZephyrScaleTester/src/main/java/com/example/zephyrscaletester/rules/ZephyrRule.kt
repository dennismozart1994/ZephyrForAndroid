package com.example.zephyrscaletester.rules

import android.util.Log
import com.example.zephyrscaletester.repository.ZephyrRepository
import com.example.zephyrscaletester.TestExecutionStatus
import com.example.zephyrscaletester.notation.ZephyrCase
import org.junit.AssumptionViolatedException
import org.junit.Ignore
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.Date

/**
 * Testing Rule to interact with Zephyr Scale if @ZephyrCase notation is provided to a test
 * @param jiraProject: Project in JIRA that holds your Zephyr Scale instance.
 * E.g: For the test EXMPL-123 the project would be EXMPL
 * @param authorizationToken JWT Bearer token used to authenticate with Zephyr
 * @param userID: Zephyr JIRA user ID that will be responsible to interact with Jira
 * @param testCycleFolderID: Folder ID to create Test cycles in
 * @param testCycleTitle: Test Cycle Title name to set on Zephyr Scale when creating a test cycle
 * @param testCycleDescription: Test Cycle description to set on Zephyr scale when creating a test cycle
 * @param existingTestCycleID: Test cycle ID to use in case of running tests in an already created test cycle on Zephyr Scale
 */
class ZephyrRule(
    jiraProject: String,
    authorizationToken: String,
    userID: String,
    testCycleFolderID: Int? = null,
    testCycleTitle: String? = null,
    testCycleDescription: String? = null,
    existingTestCycleID: String? = null
): TestWatcher() {
    private var zephyrCycleID: String = existingTestCycleID ?: ""
    private val zephyrHandler = ZephyrRepository(jiraProject, authorizationToken, userID, testCycleFolderID ?: 0)
    private var startTime: Long = 0
    private var endTime: Long = 0

    init {
        if(zephyrCycleID.isEmpty()) {
            assert(testCycleFolderID != null) {
                Log.d("ZEPHYR_RULE", "Folder ID to create test cycle was not provided")
            }
            zephyrCycleID = zephyrHandler.createTestCycle(
                testCycleTitle ?: "Zephyr Automated Cycle",
                testCycleDescription ?: "Test Cycle created through Zephyr Rule"
            )
        }
    }

    override fun starting(description: Description) {
        startTime = Date().time
        super.starting(description)
    }

    override fun succeeded(description: Description) {
        super.succeeded(description)
        endTime = Date().time
        val methodName = description.methodName
        val className = description.className

        val testName = "$className / $methodName()"
        for (testId in getZephyrInfo((description))) {
            val resultId = zephyrHandler.addTestResult(testId, zephyrCycleID, TestExecutionStatus.PASSED.value,
                "Passed by the automation", endTime - startTime, testName)
            if (resultId > 0) {
                println("Test Result added to Zephyr successfully for $testId, result ID: $resultId")
            } else {
                println("Error trying to add result to Zephyr for $testId")
            }
        }
    }

    override fun failed(e: Throwable, description: Description) {
        super.failed(e, description)
        val methodName = description.methodName
        val className = description.className

        // Zephyr Integration
        endTime = Date().time
        val testName = "$className / $methodName()"
        val comment = "<br/><b>Error:</b> <i>${e.toString()}</i><br/><br/>" +
                "<b>Full Stack Trace:</b><br/><i>${e.stackTraceToString()}</i>"
        for (testId in getZephyrInfo(description)) {
            val resultId = zephyrHandler.addTestResult(testId, zephyrCycleID, TestExecutionStatus.FAILED.value,
                comment, endTime - startTime, testName)
            if (resultId > 0) {
                println("Test Result added to Zephyr successfully for $testId, result ID: $resultId")
            } else {
                println("Error trying to add result to Zephyr for $testId")
            }
        }
    }

    override fun skipped(e: AssumptionViolatedException, description: Description) {
        super.skipped(e, description)
        val ignore: Ignore = description.getAnnotation(Ignore::class.java) as Ignore
        val ignoreMessage = java.lang.String.format(
            "@Ignore test method '%s()': '%s'",
            description.methodName, ignore.value)

        endTime = Date().time
        val methodName = description.methodName
        val className = description.className

        val testName = "$className / $methodName()"
        for (testId in getZephyrInfo((description))) {
            val resultId = zephyrHandler.addTestResult(testId, zephyrCycleID, TestExecutionStatus.BLOCKED.value,
                "Blocked due to: $ignoreMessage", endTime - startTime, testName)
            if (resultId > 0) {
                println("Test Result added to Zephyr successfully for $testId, result ID: $resultId")
            } else {
                println("Error trying to add result to Zephyr for $testId")
            }
        }

    }

    private fun getZephyrInfo(description: Description): Array<String> {
        val caseAnnotation = description.getAnnotation(ZephyrCase::class.java)
        if (caseAnnotation != null) {
            return caseAnnotation.testIds
        }
        return arrayOf()
    }
}