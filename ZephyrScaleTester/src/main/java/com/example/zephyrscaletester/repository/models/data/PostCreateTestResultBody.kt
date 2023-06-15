package com.example.zephyrscaletester.repository.models.data

data class PostCreateTestResultBody(
    val assignedToId: String,
    val comment: String,
    val executedById: String,
    val executionTime: Long,
    val projectKey: String,
    val statusName: String,
    val testCaseKey: String,
    val testCycleKey: String
)
