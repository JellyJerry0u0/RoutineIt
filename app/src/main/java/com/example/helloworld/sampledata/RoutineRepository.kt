package com.example.helloworld.sampledata

import android.content.Context
import com.example.helloworld.ui.screen.Routine
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

object RoutineRepository {
    fun loadRoutinesFromAssets(context: Context): List<Routine> {
        val json = context.assets.open("routines.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Routine>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun loadRoutinesFromInternal(context: Context): List<Routine> {
        val file = File(context.filesDir, "routines.json")
        if (!file.exists()) return emptyList()
        val json = file.readText()
        val type = object : com.google.gson.reflect.TypeToken<List<Routine>>() {}.type
        return com.google.gson.Gson().fromJson(json, type)
    }

    fun saveRoutinesToInternal(context: Context, routines: List<Routine>) {
        val file = File(context.filesDir, "routines.json")
        val json = com.google.gson.Gson().toJson(routines)
        file.writeText(json)
    }

    fun copyRoutinesJsonToInternalIfNeeded(context: Context) {
        val file = File(context.filesDir, "routines.json")
        if (!file.exists()) {
            context.assets.open("routines.json").use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}