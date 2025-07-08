// data/RoutineEntry.kt
package com.example.helloworld.data

data class RoutineEntry(
    val routineName: String,
    val date: String,         // 예: "2025-07-07"
    val imageUri: String,     // 이미지 경로 (Uri.toString())
    val memo: String,          // 사용자 작성 문장
    val owner : String
)
