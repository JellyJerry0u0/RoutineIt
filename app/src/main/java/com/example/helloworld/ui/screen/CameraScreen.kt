package com.example.helloworld.ui.screen

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.helloworld.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

var cameraImageUri: Uri? = null
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val cameraLauncher =  rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            //여기서 cameraImageUri에 있는 사진을 처리할 수 있음
        }

    }


    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한이 허용되었으면 카메라 실행
            val photoFile = createImageFile(context)
            cameraImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                photoFile
            )
            cameraLauncher.launch(cameraImageUri)
        }
    }

    val hasCameraPermission = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED




    var category by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val data = loadJsonFromAssets(context, "routines.json")
        val jsonArray = JSONArray(data)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.getString("category") == "운동") {
                category = obj.getString("category")
                time = obj.getString("time")
                name = obj.getString("name")
                break
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text("인증 중인 루틴", fontSize = 26.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)

        Spacer(modifier = Modifier.height(12.dp))

        Image(
            painter = painterResource(id = R.drawable.exercise),
            contentDescription = "운동 이미지",
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text("$time", fontSize = 30.sp, color = Color.Black, modifier = Modifier.align(Alignment.End))

        Spacer(modifier = Modifier.height(6.dp))

        Text(name, fontSize = 40.sp, color = Color.Black, modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(32.dp))

        Image(
            painter = painterResource(id = R.drawable.camera),
            contentDescription = "카메라 아이콘",
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.CenterHorizontally)
                .clickable {
                    if (hasCameraPermission) {
                        val photoFile = createImageFile(context)
                        cameraImageUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            photoFile
                        )
                        cameraLauncher.launch(cameraImageUri)
                    } else {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "사진을 찍고,\n한 마디를 남겨보세요.",
            fontSize = 27.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

suspend fun loadJsonFromAssets(context: Context, filename: String): String = withContext(Dispatchers.IO) {
    val inputStream = context.assets.open(filename)
    val reader = BufferedReader(inputStream.reader())
    val content = StringBuilder()
    var line: String? = reader.readLine()
    while (line != null) {
        content.append(line)
        line = reader.readLine()
    }
    content.toString()
}
fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_", ".jpg", storageDir
    )
}
