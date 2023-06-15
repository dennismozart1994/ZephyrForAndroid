package com.example.zephyrscaletester.repository.models.data

data class PostCreateTestCycleBody(
    val description: String,
    val folderId: Int,
    val name: String,
    val projectKey: String
)
