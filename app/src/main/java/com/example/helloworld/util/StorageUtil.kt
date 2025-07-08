package com.example.helloworld.util

import android.content.Context
import java.io.File

fun copyRoutineEntriesFromAssetsIfNotExists(context: Context) {
    val file = File(context.filesDir, "routine_entries.json")
    if (!file.exists()) {
        context.assets.open("routine_entries.json").use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}
