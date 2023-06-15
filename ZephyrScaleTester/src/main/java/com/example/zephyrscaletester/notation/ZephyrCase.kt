package com.example.zephyrscaletester.notation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ZephyrCase(val testIds: Array<String>)