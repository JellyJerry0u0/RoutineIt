package com.example.helloworld.storage


import android.content.Context
import android.net.Uri
import android.util.Log
//import androidx.compose.runtime.changelist.Operation.AdvanceSlotsBy.name
import androidx.core.content.FileProvider
import com.example.helloworld.data.RoutineEntry
import com.example.helloworld.ui.screen.RoutineItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

object RoutineStorage {

    private const val FILE_NAME = "routine_entries.json"
    private val gson = Gson()

    // 전체 불러오기
    fun loadEntries(context: Context): List<RoutineEntry> {
        val gson = Gson()
        val type = object : TypeToken<List<RoutineEntry>>() {}.type

        // 1) assets 에 있는 “더미” JSON 읽기
        val assetJson = context.assets.open(FILE_NAME).bufferedReader().use { it.readText() }
        val assetList: List<RoutineEntry> = gson.fromJson(assetJson, type)

        // 2) internal 에 저장된 “동적” JSON 읽기 (없으면 빈 리스트)
        val internalFile = File(context.filesDir, FILE_NAME)
        val internalList: List<RoutineEntry> = if (internalFile.exists()) {
            gson.fromJson(internalFile.readText(), type)
        } else emptyList()

        // 3) 합치되, 중복(entry.date + imageUri)이 있다면 internal 쪽을 우선
        val combinedMap = (assetList + internalList).associateBy { it.date + it.imageUri }
        val combinedList = combinedMap.values.toList()

        // 4) 기존과 동일하게 URI 포맷으로 변환
        return combinedList.map { entry ->
            val uriString = if (entry.imageUri.startsWith("content://") || entry.imageUri.startsWith("file://")) {
                entry.imageUri
            } else {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    File(context.filesDir, "images/${entry.imageUri}")
                ).toString()
            }
            entry.copy(imageUri = uriString)
        }
    }




    // 추가하고 저장
    fun addEntry(context: Context, newEntry: RoutineEntry) {
        val currentEntries = loadEntries(context).toMutableList()
        currentEntries.add(newEntry)
        saveEntries(context, currentEntries)
    }

    // 저장
    private fun saveEntries(context: Context, entries: List<RoutineEntry>) {
        val json = gson.toJson(entries)
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(json)
    }

    /*
    fun copyRoutineJsonIfNotExists(context: Context) {
        val targetFile = File(context.filesDir, "routine_entries.json")
        if (!targetFile.exists()) {
            context.assets.open("routine_entries.json").use { inputStream ->
                FileOutputStream(targetFile).use { output ->
                    inputStream.copyTo(output)
                }
            }
        }
    }
     */

    fun copyRoutineImagesFromAssetsIfNotExists(context: Context) {
        val assetManager = context.assets
        // assets/routine_images 폴더 내 모든 파일 이름을 읽어옵니다
        val assetFiles = assetManager.list("routine_images") ?: return

        // internal images 폴더 준비
        val destDir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }

        // 하나씩 검사하며, 없으면 복사
        for (fileName in assetFiles) {
            val outFile = File(destDir, fileName)
            if (!outFile.exists()) {
                assetManager.open("routine_images/$fileName").use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }


    fun copyRoutineJsonIfNotExists(context: Context) {
        val targetFile = File(context.filesDir, "routine_entries.json")
        if (!targetFile.exists()) {
            context.assets.open("routine_entries.json").use { inputStream ->
                FileOutputStream(targetFile).use { output ->
                    inputStream.copyTo(output)
                }
            }
        }
    }

    fun loadJoinedRoutines(context: Context): List<RoutineItem> {
        val file = File(context.filesDir, "routines.json")
        val json = file.bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<RoutineItem>>() {}.type
        return Gson().fromJson<List<RoutineItem>>(json, type).filter { it.isJoined }
    }


    fun getImageUri(context: Context, fileName: String): Uri {
        val file = File(context.filesDir, "images/$fileName")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }


}
