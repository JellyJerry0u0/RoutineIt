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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
import coil.compose.AsyncImage
import com.example.helloworld.R
import com.example.helloworld.data.RoutineEntry
import com.example.helloworld.storage.RoutineStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.threeten.bp.LocalDate
import java.io.BufferedReader
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.itemsIndexed

var cameraImageUri: Uri? = null
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val routines = remember { loadJoinedRoutinesFromAssets(context) }
    var selectedIndex by remember { mutableStateOf(0) }
    val selectedRoutine = routines.getOrNull(selectedIndex)
    var showDialog by remember { mutableStateOf(false) }
    var memo by remember { mutableStateOf("") }
    val cameraLauncher =  rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            showDialog = true // 사진 찍으면 팝업 띄우기
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
            /*
            if (obj.getString("category") == "운동") {
                category = obj.getString("category")
                time = obj.getString("time")
                name = obj.getString("name")
                break
            }

             */
            category = obj.getString("category")
            time = obj.getString("time")
            name = obj.getString("name")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "오늘의 루틴",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = selectedRoutine?.time ?: "--:--",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ─── 내 루틴 리스트 (가로 스크롤) ────────────────────
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            itemsIndexed(routines) { index, routine ->
                // 클릭하면 선택 루틴이 바뀌어 위의 시간도 바뀜
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .background(
                            if (index == selectedIndex) Color(0xFFDDDDFF) else Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedIndex = index }
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = routine.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = routine.description,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))



        Spacer(modifier = Modifier.height(10.dp))

        Text("오늘의 루틴", fontSize = 20.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)

        Spacer(modifier = Modifier.height(12.dp))
/*
        Image(
            painter = painterResource(id = R.drawable.exercise),
            contentDescription = "운동 이미지",
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.Start)
        )

 */

        //Spacer(modifier = Modifier.height(40.dp))

        //Text("$time", fontSize = 30.sp, color = Color.Black, modifier = Modifier.align(Alignment.End))

        //Spacer(modifier = Modifier.height(6.dp))

        //Text(name, fontSize = 40.sp, color = Color.Black, modifier = Modifier.align(Alignment.CenterHorizontally))

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

    if (showDialog && cameraImageUri != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = {
                    // 1) cameraImageUri 에 담긴 “temp” 외부 파일의 이름을 그대로 꺼냅니다
                    val srcFileName = cameraImageUri!!.lastPathSegment!!

                    // 2) internal/images 디렉터리 준비
                    val imagesDir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
                    val dstFile = File(imagesDir, srcFileName)

                    // 3) external URI → internal 파일로 복사
                    context.contentResolver.openInputStream(cameraImageUri!!)?.use { input ->
                        dstFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    // 4) 복사된 internal 파일에 대한 provider URI 생성
                    val savedUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        dstFile
                    )

                    val routineNameToSave = selectedRoutine?.name
                        ?: routines.getOrNull(selectedIndex)?.name
                        ?: "unknown"

                    // 5) JSON 에는 이 savedUri 를 문자열로 저장
                    val entry = RoutineEntry(
                        routineName = routineNameToSave,
                        date        = LocalDate.now().toString(),
                        imageUri    = savedUri.toString(),
                        memo        = memo,
                        owner       = "내 루틴"
                    )
                    RoutineStorage.addEntry(context, entry)

                    // 리셋
                    memo = ""
                    showDialog = false
                }) {
                    Text("저장")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("취소")
                }
            },
            title = { Text("루틴 인증") },
            text = {
                Column {
                    AsyncImage(
                        model = cameraImageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = memo,
                        onValueChange = { if (it.length <= 50) memo = it },
                        label = { Text("한 마디 (50자 이내)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
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


