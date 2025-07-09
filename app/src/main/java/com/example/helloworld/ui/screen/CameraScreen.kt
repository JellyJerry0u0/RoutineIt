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
import com.example.helloworld.sampledata.RoutineRepository
import com.example.helloworld.ui.theme.Paperlogy
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.with
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.example.helloworld.ui.screen.AnimatedHighlighter
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime
import org.threeten.bp.Duration



var cameraImageUri: Uri? = null
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    var routines by remember { mutableStateOf<List<Routine>>(emptyList()) }
    var selectedIndex by remember { mutableStateOf(0) }
    //val selectedRoutine = routines.getOrNull(selectedIndex)
    var showDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var memo by remember { mutableStateOf("") }
    val cameraLauncher =  rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            showDialog = true // 사진 찍으면 팝업 띄우기
        }

    }



    var showNotTimeDialog by remember { mutableStateOf(false) }


    // routines가 이미 시간 순으로 정렬되어 있으므로, filter만 해 주면 joinedRoutines도 시간순
    val joinedRoutines = routines.filter { it.isJoined }

    val selectedRoutine = joinedRoutines.getOrNull(selectedIndex)
    // 선택된 루틴 시간이 현재 기준 ±10분 내인지
    fun isNearAuthTime(routine: Routine?): Boolean {
        routine ?: return false
        val now = LocalTime.now()
        val target = parseRoutineTime(routine.time)
        return Duration.between(now, target).abs().toMinutes() <= 10
    }

    val totalCount = joinedRoutines.size
    var showAlreadyDoneDialog by remember { mutableStateOf(false) }


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

    //var routines by remember { mutableStateOf<List<Routine>>(emptyList()) }
    var entries by remember { mutableStateOf<List<RoutineEntry>>(emptyList()) } // ← 오늘자 인증 기록 로드용 추가
    //var selectedIndex by remember { mutableStateOf(0) }

    //var hasAuthTimeState by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    //val joinedRoutines = routines.filter { it.isJoined }
    val completedSet = entries.map { it.routineName }.toSet() // ← 완료된 루틴 이름 모음
    LaunchedEffect(Unit) {
        val data = loadJsonFromAssets(context, "routines.json")
        val jsonArray = JSONArray(data)
        val allRoutines = RoutineRepository.loadRoutinesFromInternal(context)

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

        val today = when (LocalDate.now().dayOfWeek) {
            DayOfWeek.MONDAY    -> "월"
            DayOfWeek.TUESDAY   -> "화"
            DayOfWeek.WEDNESDAY -> "수"
            DayOfWeek.THURSDAY  -> "목"
            DayOfWeek.FRIDAY    -> "금"
            DayOfWeek.SATURDAY  -> "토"
            DayOfWeek.SUNDAY    -> "일"
        }

        val filtered = allRoutines.filter { routine ->
            today in routine.days  // routine.days: List<String>
        }
        routines = filtered.sortedBy { parseRoutineTime(it.time) }

        //routines = RoutineRepository.loadRoutinesFromInternal(context)
        entries = RoutineStorage
            .loadEntries(context)
            .filter { it.date == LocalDate.now().toString() }
/*
        val loaded = RoutineRepository.loadRoutinesFromInternal(context)
        routines = loaded.sortedBy { parseRoutineTime(it.time) }
        entries = RoutineStorage
            .loadEntries(context)
            .filter { it.date == LocalDate.now().toString() }
            */


        // 2) joinedRoutines 정의
        val joined = routines.filter { it.isJoined }

        // 3) 현재 시각 계산
        val now = LocalTime.now()



        val nearIdx = joined.indexOfFirst { isNearAuthTime(it) }
        // 없으면 이후 루틴 중 가장 가까운 것
        val futureIdx = joined.indexOfFirst { parseRoutineTime(it.time).isAfter(now) }
        selectedIndex = when {
            nearIdx >= 0   -> nearIdx
            futureIdx >= 0 -> futureIdx
            else           -> 0
        }





    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 24.dp, top = 70.dp, end = 24.dp, bottom = 24.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(37.dp))
            Text(
                text = "오늘의 루틴",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Paperlogy
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = selectedRoutine?.time ?: "--:--",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Paperlogy
            )
            Spacer(modifier = Modifier.width(37.dp))

        }

        // 선택된 루틴
        val selectedRoutine = joinedRoutines.getOrNull(selectedIndex)

// 2) 이미 완료된 루틴인가?
        val isSelectedDone = selectedRoutine?.let { completedSet.contains(it.name) } == true
        // 1) 인증 가능 시간대인가?
        val isSelectedNear = !isSelectedDone && isNearAuthTime(selectedRoutine)

        val now = LocalTime.now()
// ➋ 지난 루틴 = 인증 가능도 아니고, 완료도 아니면서, 루틴 시간이 이미 지남
        val isSelectedPast = !isSelectedNear
                && !isSelectedDone
                && selectedRoutine
            ?.let { parseRoutineTime(it.time).isBefore(now) }
            .orFalse()


        RoutineSwiper(joinedRoutines = joinedRoutines,
            selectedIndex = selectedIndex,
            onChangeIndex = { selectedIndex = it },
            //completedSet = completedSet,
            isNearAuth       = isSelectedNear,   // ← 추가
            isDone           = isSelectedDone,
            isPast           = isSelectedPast
        )



        Spacer(modifier = Modifier.height(24.dp))



        Spacer(modifier = Modifier.height(10.dp))

        //Text("오늘의 루틴", fontSize = 20.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)

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

        Spacer(modifier = Modifier.height(10.dp))

        Image(
            painter = painterResource(id = R.drawable.camera_2),
            contentDescription = "카메라 아이콘",
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.CenterHorizontally)
                .clickable {
                    when {
                        completedSet.contains(selectedRoutine?.name) ->
                            showAlreadyDoneDialog = true

                        isNearAuthTime(selectedRoutine) -> {
                            // 권한 처리 + 카메라 실행
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

                        else ->
                            showNotTimeDialog = true
                    }
                }
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "사진을 찍고,\n한 마디를 남겨보세요.",
            fontSize = 23.sp,
            color = Color.Gray,
            fontFamily = Paperlogy,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showDialog && cameraImageUri != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            // 1) 팝업 전체를 라운드 사각형으로
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            // 2) 제목 영역 (가운데 텍스트 + 우측 X 버튼)
            title = {
                Box(Modifier.fillMaxWidth()) {
                    Text(
                        text = "루틴 인증",
                        fontFamily = Paperlogy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "닫기",
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                            .clickable { showDialog = false }
                    )
                }
            },
            // 3) 본문: 둥근 사각 이미지 + 메모 필드
            // … AlertDialog 안 text = { … } 부분에서

            text = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(12.dp))       // 둥근 사각 클립
                            .background(Color.LightGray)           // 로딩 전/실패 시 배경
                    ) {
                        AsyncImage(
                            model = cameraImageUri,
                            contentDescription = "인증 사진",
                            contentScale = ContentScale.Crop,     // 꽉 채워서 잘라냄
                            modifier = Modifier.matchParentSize() // Box 크기에 맞춤
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = memo,
                        onValueChange = { if (it.length <= 50) memo = it },
                        label = { Text("한 마디 (50자 이내)",fontWeight = FontWeight.SemiBold,
                            fontFamily = Paperlogy) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor   = Color.DarkGray,
                            cursorColor          = Color.DarkGray,
                            focusedLabelColor    = Color.DarkGray,
                            unfocusedLabelColor  = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },

            // 4) 확인 버튼만, 텍스트 변경
            confirmButton = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            // 저장 로직 …
                            val srcFileName = cameraImageUri!!.lastPathSegment!!
                            val imagesDir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
                            val dstFile = File(imagesDir, srcFileName)
                            context.contentResolver.openInputStream(cameraImageUri!!)?.use { input ->
                                dstFile.outputStream().use { output -> input.copyTo(output) }
                            }
                            val savedUri = FileProvider.getUriForFile(
                                context, "${context.packageName}.provider", dstFile
                            )
                            val entry = RoutineEntry(
                                routineName = selectedRoutine?.name ?: "unknown",
                                date = LocalDate.now().toString(),
                                imageUri = savedUri.toString(),
                                memo = memo,
                                owner = "내 루틴"
                            )
                            RoutineStorage.addEntry(context, entry)
                            entries = RoutineStorage.loadEntries(context)
                                .filter { it.date == LocalDate.now().toString() }
                            memo = ""
                            showDialog = false
                            showSuccessDialog = true
                            //showDialog = false
                        },
                        modifier = Modifier
                            .width(200.dp)
                            .height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF72ADFF),
                            contentColor = Color.White
                        )
                    ) {
                        Text("루틴 인증 등록",
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Paperlogy)
                    }
                }
            },
            // 5) 취소 버튼 제거
            dismissButton = null

        )
    }

    //성공 알림 다이얼로그
    if (showSuccessDialog) {
        // 1. 띄우고
        AlertDialog(
            onDismissRequest = { /* 자동 닫힘까지 잠금 */ },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            text = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedHighlighterCamera(text = selectedRoutine?.name ?: "")
                    Spacer(Modifier.height(12.dp))
                    Text("오늘 루틴 완료! \uD83D\uDE0A", fontWeight = FontWeight.Bold, color = Color.Black,fontFamily = Paperlogy, fontSize = 24.sp)
                }
            },
            confirmButton = { /* 비워둠 */ },
            dismissButton = null
        )
        // 2. 자동 닫기
        LaunchedEffect(Unit) {
            delay(2000)                // 애니메이션+텍스트 머무는 시간 (원래 1800)
            showSuccessDialog = false
        }
    }

    if (showAlreadyDoneDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = null,
            text = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        //.fillMaxHeight()
                        .padding(vertical = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "이미 인증 완료된 루틴입니다",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontFamily = Paperlogy
                    )
                    Spacer(Modifier.height(12.dp))
                    CheckmarkAnimation(
                        sizeDp = 48.dp,                  // 크기
                        strokeWidth = 8f,                // 선 굵기
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    //Spacer(Modifier.height(16.dp))
                }
            },
            confirmButton = {  },
            dismissButton = {  },
            shape = RoundedCornerShape(12.dp),
            containerColor = Color.White
        )
        LaunchedEffect(showAlreadyDoneDialog) {
            delay(1800)
            showAlreadyDoneDialog = false
        }
    }

    if (showNotTimeDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = null,
            text = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        //.fillMaxHeight()
                        .padding(vertical = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "인증 시간이 아닙니다",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontFamily = Paperlogy
                    )
                    Spacer(Modifier.height(12.dp))
                    CrossAnimation(
                        sizeDp = 48.dp,
                        strokeWidth = 8f,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    //Spacer(Modifier.height(16.dp))
                }
            },
            confirmButton = {  },
            dismissButton = {  },
            shape = RoundedCornerShape(12.dp),
            containerColor = Color.White
        )
        LaunchedEffect(showNotTimeDialog) {
            delay(1800)
            showNotTimeDialog = false
        }
    }





    /*
    if (showDialog && cameraImageUri != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            // 1) title 부분: 가운데 “루틴 인증”, 우측 상단에 X 아이콘
            title = {
                Box(Modifier.fillMaxWidth()) {
                    Text(
                        text = "루틴 인증",
                        fontFamily = Paperlogy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                            .clickable { showDialog = false }
                    )
                }
            },
            // 2) 본문: 정사각 라운드 이미지 + 메모 입력 필드
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = cameraImageUri,
                        contentDescription = "인증 사진",
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = memo,
                        onValueChange = { if (it.length <= 50) memo = it },
                        label = { Text("한 마디 (50자 이내)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            // 3) 버튼: “오늘 루틴 완료!” 하나만
            confirmButton = {
                Button(
                    onClick = {
                        // 저장 로직 …
                        val srcFileName = cameraImageUri!!.lastPathSegment!!
                        val imagesDir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
                        val dstFile = File(imagesDir, srcFileName)
                        context.contentResolver.openInputStream(cameraImageUri!!)?.use { input ->
                            dstFile.outputStream().use { output -> input.copyTo(output) }
                        }
                        val savedUri = FileProvider.getUriForFile(
                            context, "${context.packageName}.provider", dstFile
                        )
                        val entry = RoutineEntry(
                            routineName = selectedRoutine?.name ?: "unknown",
                            date = LocalDate.now().toString(),
                            imageUri = savedUri.toString(),
                            memo = memo,
                            owner = "내 루틴"
                        )
                        RoutineStorage.addEntry(context, entry)
                        entries = RoutineStorage.loadEntries(context)
                            .filter { it.date == LocalDate.now().toString() }
                        memo = ""
                        showDialog = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("오늘 루틴 완료!")
                }
            },
            // 4) 취소 버튼 제거
            dismissButton = null,
            modifier = Modifier.background(Color.White)
        )
    }

     */

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RoutineSwiper(
    joinedRoutines: List<Routine>,
    selectedIndex: Int,
    onChangeIndex: (Int) -> Unit,
    isNearAuth : Boolean,
    isDone : Boolean,
    isPast: Boolean
) {
    val selectedRoutine = joinedRoutines.getOrNull(selectedIndex)
    val totalCount = joinedRoutines.size
    val arrowArea = 32.dp
    val arrowOffset = 16.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(arrowArea)
                .offset(x = -arrowOffset)
                .clickable(enabled = selectedIndex > 0) {
                    if (selectedIndex > 0) onChangeIndex(selectedIndex - 1)
                },
            contentAlignment = Alignment.Center
        ) {
            if (selectedIndex > 0) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Outlined.ChevronLeft,
                    contentDescription = "이전 달",
                    tint = Color(0xFFB0B0B0),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        AnimatedContent(
            targetState = selectedRoutine,
            transitionSpec = {
                val direction = if (targetState != initialState &&
                    joinedRoutines.indexOf(targetState) > joinedRoutines.indexOf(initialState)
                ) 1 else -1

                (slideInHorizontally { width -> direction * width } + fadeIn())
                    .with(slideOutHorizontally { width -> -direction * width } + fadeOut())
            },
            modifier = Modifier
                .width(250.dp)
                .height(140.dp)
        ) { routine ->
            if (routine != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF9FBFF), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            if (isDone) {
                                AnimatedHighlighterCamera(text = routine.name)
                            } else {
                                Text(
                                    text = routine.name,
                                    fontSize = 18.sp,
                                    fontFamily = Paperlogy,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            Spacer(Modifier.width(6.dp))

                            when {
                                isDone -> {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFD6E6FF), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "완료",
                                            fontFamily = Paperlogy,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF90B4E6)
                                        )
                                    }
                                }
                                isNearAuth -> {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFD6E6FF), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "인증 가능!",
                                            fontFamily = Paperlogy,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF90B4E6)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = routine.description,
                            fontFamily = Paperlogy,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            maxLines = 3
                        )


                    }

                    //
                    if (!isNearAuth && !isDone) {
                        val label = if (isPast) "지난 루틴" else "다가오는 루틴"
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            fontFamily = Paperlogy,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 8.dp, bottom = 8.dp)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .size(arrowArea)
                .offset(x = arrowOffset)
                .clickable(enabled = selectedIndex < totalCount - 1) {
                    if (selectedIndex < totalCount - 1) onChangeIndex(selectedIndex + 1)
                },
            contentAlignment = Alignment.Center
        ) {
            if (selectedIndex < totalCount - 1) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Outlined.ChevronRight,
                    contentDescription = "이후 달",
                    tint = Color(0xFFB0B0B0),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

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

@Composable
fun AnimatedHighlighterCamera(text: String) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedWidth by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "highlight_width"
    )

    LaunchedEffect(text) {
        animationPlayed = false
        delay(400)      // 너무 길게 주지 말고
        animationPlayed = true
    }

    // Text 에 drawBehind 를 직접 걸어서, 절대 위치 오프셋 없이 딱 맞춰줌
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF35537C),
        fontFamily = Paperlogy,
        modifier = Modifier
            .wrapContentSize()           // 텍스트 크기만큼만
            .graphicsLayer { clip = true }
            .padding(vertical = 2.dp)
            .drawBehind {
                // 텍스트 전체 높이에 딱 맞춰 그리기
                val h = size.height
                drawRect(
                    color = Color(0x8069A6F7), // 50% 불투명도 적용된 #69A6F7
                    topLeft = Offset.Zero,
                    size = Size(size.width * animatedWidth, size.height)
                )
            }
    )
}

@Composable
fun CheckmarkAnimation(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 48.dp,
    strokeWidth: Float = 8f,       // ← 선 굵기
    color: Color = Color(0xFF72ADFF)
) {
    // 애니메이션 진행값 (0f..1f)
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 원 그리기 (0→1), 잠시 멈춤, 체크 그리기 (1→2)
        progress.animateTo(1f, animationSpec = tween(600))
        delay(200)
        progress.animateTo(2f, animationSpec = tween(400))
    }

    Canvas(
        modifier = modifier
            .size(sizeDp)
    ) {
        val w = size.width
        val h = size.height

        // 1) 먼저 원
        if (progress.value <= 1f) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress.value,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
        } else {
            // 2) 원 완성된 채로
            drawCircle(color = color, radius = w/2, style = Stroke(width = strokeWidth))

            // 체크 경로 그리기
            val t = (progress.value - 1f).coerceIn(0f, 1f)
            // 체크 좌표 (사이즈에 따라 비례)
            val start = Offset(x = w * 0.30f, y = h * 0.55f)
            val mid   = Offset(x = w * 0.45f, y = h * 0.70f)
            val end   = Offset(x = w * 0.75f, y = h * 0.35f)

            // 중간점 이동
            if (t < 0.5f) {
                val tt = t / 0.5f
                drawLine(color, start,  Offset(
                    lerp(start.x, mid.x, tt),
                    lerp(start.y, mid.y, tt)
                ), strokeWidth)
            } else {
                drawLine(color, start, mid, strokeWidth)
                val tt = (t - 0.5f) / 0.5f
                drawLine(color, mid, Offset(
                    lerp(mid.x, end.x, tt),
                    lerp(mid.y, end.y, tt)
                ), strokeWidth)
            }
        }
    }
}

// lerp helper
private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
private fun Boolean?.orFalse() = this == true


private fun parseRoutineTime(timeStr: String): LocalTime {
    val parts = timeStr.split(" ")
    val ampm = parts[0]
    val (h, m) = parts[1].split(":").let { it[0].toInt() to it[1].toInt() }
    var hour = h
    if (ampm == "오후" && hour < 12) hour += 12
    if (ampm == "오전" && hour == 12) hour = 0
    return LocalTime.of(hour, m)
}


@Composable
fun CrossAnimation(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 48.dp,
    strokeWidth: Float = 8f,
    color: Color = Color(0xFFFF7272)
) {
    // 0f → 1f : 원, 1f → 2f : 가위표(X)
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(600))
        delay(200)
        progress.animateTo(2f, animationSpec = tween(600))
    }

    Canvas(modifier = modifier.size(sizeDp)) {
        val w = size.width
        val h = size.height

        if (progress.value <= 1f) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress.value,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
        } else {
            // 완전한 원
            drawCircle(
                color = color,
                radius = w.coerceAtMost(h) / 2f,
                style = Stroke(width = strokeWidth)
            )

            // X 그리기
            val t = (progress.value - 1f).coerceIn(0f, 1f)
            val start1 = Offset(x = w * 0.25f, y = h * 0.25f)
            val end1 = Offset(x = w * 0.75f, y = h * 0.75f)
            val start2 = Offset(x = w * 0.75f, y = h * 0.25f)
            val end2 = Offset(x = w * 0.25f, y = h * 0.75f)

            if (t < 0.5f) {
                // 첫 번째 대각선
                val tt = t / 0.5f
                val currentEnd = Offset(
                    lerp(start1.x, end1.x, tt),
                    lerp(start1.y, end1.y, tt)
                )
                drawLine(
                    color = color,
                    start = start1,
                    end = currentEnd,
                    strokeWidth = strokeWidth
                )
            } else {
                // 첫 번째 대각선 완성
                drawLine(
                    color = color,
                    start = start1,
                    end = end1,
                    strokeWidth = strokeWidth
                )
                // 두 번째 대각선
                val tt = (t - 0.5f) / 0.5f
                val currentEnd = Offset(
                    lerp(start2.x, end2.x, tt),
                    lerp(start2.y, end2.y, tt)
                )
                drawLine(
                    color = color,
                    start = start2,
                    end = currentEnd,
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}

// lerp helper
//private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
