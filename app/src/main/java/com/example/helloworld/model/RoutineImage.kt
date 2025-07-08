package com.example.helloworld.sampledata

import org.threeten.bp.LocalDate

data class RoutineImage(
    val routineName: String,
    val date: LocalDate,
    val imageUri: String,
    val memo: String? = null
)