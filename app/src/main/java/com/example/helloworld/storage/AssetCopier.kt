package com.example.helloworld.storage

// 파일 위치: com.example.helloworld.storage.AssetCopier.kt


import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object AssetCopier {
    fun copyRoutineImagesFromAssetsIfNotExists(context: Context) {
        val imageDir = File(context.filesDir, "images")
        if (!imageDir.exists()) imageDir.mkdirs()

        val assetManager = context.assets
        val existingFiles = imageDir.list()?.toSet() ?: emptySet()

        val imageNames = assetManager.list("routine_images")?.toList() ?: emptyList()  // ✅ 동적으로 파일명 가져옴!
        Log.d("AssetCopier", "Found assets: $imageNames")

        imageNames.forEach { name ->
            if (!existingFiles.contains(name)) {
                Log.d("AssetCopier", "Copying asset → $name")
                assetManager.open("routine_images/$name").use { input ->
                    File(imageDir, name).outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}
