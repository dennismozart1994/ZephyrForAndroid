package com.example.zephyrscaletester

/**
 * Results Constants for Zephyr API Instance
 * @param value: Execution status string value at the Zephyr Instance
 */
enum class TestExecutionStatus(val value: String) {
    BLOCKED("Blocked"),
    FAILED("Fail"),
    IN_PROGRESS("In Progress"),
    NOT_EXECUTED("Not Executed"),
    PASSED("Pass")
}