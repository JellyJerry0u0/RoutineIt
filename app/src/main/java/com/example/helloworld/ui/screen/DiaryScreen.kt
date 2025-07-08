package com.example.helloworld.ui.screen

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.helloworld.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.foundation.Image
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import com.example.helloworld.ui.component.CalendarView
//import com.google.android.libraries.places.api.model.LocalDate
import kotlinx.coroutines.delay
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import org.threeten.bp.LocalDate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.helloworld.data.RoutineEntry
import com.example.helloworld.storage.AssetCopier
import com.example.helloworld.storage.RoutineStorage
import com.example.helloworld.storage.RoutineStorage.loadEntries
import com.example.helloworld.util.copyRoutineEntriesFromAssetsIfNotExists
import java.io.File
import androidx.core.net.toUri


data class RoutineItem(
    val name: String,
    val description: String,
    val days: List<String>,
    val time: String,
    val participants: Int,
    val category: String,
    val isJoined: Boolean
)

@Composable
fun DiaryScreen() {
    val context = LocalContext.current
    var joinedRoutines by remember { mutableStateOf<List<RoutineItem>>(emptyList()) }
    var selectedRoutine by remember { mutableStateOf<RoutineItem?>(null) }
    var showPopup by remember { mutableStateOf(false) }
    //var routineImages by remember { mutableStateOf<List<RoutineImage>>(emptyList()) }
    //var imagesByDate by remember { mutableStateOf<Map<LocalDate, List<RoutineImage>>>(emptyMap()) }
    //var selectedPopupImage by remember { mutableStateOf<RoutineImage?>(null) }
    //var routineImages by remember { mutableStateOf<List<RoutineEntry>>(emptyList()) }

    var selectedPopupImage by remember { mutableStateOf<RoutineEntry?>(null) }


    //val allEntries = remember { RoutineStorage.loadEntries(context) }
    var allEntries by remember { mutableStateOf<List<RoutineEntry>>(emptyList()) }
    val owners by remember(allEntries) {
        derivedStateOf {
            // allEntries가 빈 리스트면 그냥 빈 리스트를 반환해 주면 LazyRow도 안 돼서 crash 방지
            if (allEntries.isEmpty()) emptyList()
            else listOf("내 루틴") +
                    allEntries.map { it.owner }
                        .distinct()
                        .filter { it != "내 루틴" }
        }
    }
    var selectedOwner by remember { mutableStateOf("내 루틴") }

        LaunchedEffect(Unit) {
        RoutineStorage.copyRoutineImagesFromAssetsIfNotExists(context)
            val saved = File(context.filesDir, "images").listFiles()?.map { it.name }
            Log.d("CopyCheck", "Images copied: $saved")

            RoutineStorage.copyRoutineJsonIfNotExists(context)

        //joinedRoutines = RoutineStorage.loadJoinedRoutines(context)
            allEntries = RoutineStorage.loadEntries(context)

            joinedRoutines = RoutineStorage.loadJoinedRoutines(context)

        selectedRoutine = joinedRoutines.firstOrNull()
        Log.d("❓Entries", loadEntries(context).joinToString("\n") { it.date + " → " + it.imageUri })

/*
        routineImages.forEach {
            Log.d("Test", "Routine: ${it.routineName}, Uri: ${it.imageUri}")

            val imageDir = File(context.filesDir, "images")
            val files = imageDir.listFiles()
            Log.d("Test", "Images in internal storage:")
            files?.forEach { Log.d("Test", it.name) }
        }

 */


    }

    val routineImages by remember(allEntries, selectedRoutine, selectedOwner) {
        derivedStateOf {
            allEntries.filter {
                it.routineName == selectedRoutine?.name
                        && it.owner == selectedOwner
            }
        }
    }


    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "루틴 기록",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                AnimatedHighlighter(text = selectedRoutine?.name ?: "없음")

                Spacer(modifier = Modifier.width(8.dp)) // 텍스트와 아이콘 사이 여백
                Image(
                    painter = painterResource(id = R.drawable.ic_setting),
                    contentDescription = "루틴 선택",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { showPopup = true }
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            CalendarView(
                selectedDate = LocalDate.now(),
                selectedRoutine = selectedRoutine,
                routineImages = routineImages,  // 이제 RoutineEntry 리스트
                context = context,
                onDateClick = { clickedDate ->
                    // 1) 해당 날짜, 해당 루틴의 모든 entry
                    val entriesForDate = routineImages.filter {
                        LocalDate.parse(it.date) == clickedDate
                                && it.routineName == selectedRoutine?.name
                    }
                    // 2) internal/images 에 복사된 파일이 있는 entry 를 먼저
                    val preferred = entriesForDate.firstOrNull { entry ->
                        val fileName = Uri.parse(entry.imageUri).lastPathSegment
                        File(context.filesDir, "images/$fileName").exists()
                    }
                    // 3) 없으면 legacy entry 로
                    selectedPopupImage = preferred ?: entriesForDate.firstOrNull()
                }
            )





            if (selectedPopupImage != null) {
                val entry = selectedPopupImage!!
                // internal/images 에 복사본이 있는지 확인
                val fileName = Uri.parse(entry.imageUri).lastPathSegment!!
                val imageFile = File(context.filesDir, "images/$fileName")

                AlertDialog(
                    onDismissRequest = { selectedPopupImage = null },
                    title = { Text(text = entry.routineName) },
                    text = {
                        Column {
                            if (imageFile.exists()) {
                                // 1) internal 복사본이 있으면 File 모델로 바로 로드
                                AsyncImage(
                                    model = imageFile,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // 2) 없으면 fallback: content:// URI 로 로드 (with allowHardware(false))
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(entry.imageUri)
                                        .crossfade(true)
                                        .allowHardware(false)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = entry.memo)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedPopupImage = null }) {
                            Text("닫기")
                        }
                    }
                )
            }
            if (owners.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "참여자별 보기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(owners) { owner ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(80.dp)
                                .clickable { selectedOwner = owner }
                        ) {
                            // ☆ 네모 박스 & 정사각 썸네일
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color(0xFFEEEEEE), shape = RoundedCornerShape(8.dp))
                                    .padding(4.dp)
                            ) {
                                val avatar = allEntries.first { it.owner == owner }
                                val fn     = Uri.parse(avatar.imageUri).lastPathSegment!!
                                val file   = File(context.filesDir, "images/$fn")

                                AsyncImage(
                                    model        = if (file.exists()) file else Uri.parse(avatar.imageUri),
                                    contentScale = ContentScale.Crop,
                                    contentDescription = owner,
                                    modifier     = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                owner,
                                fontSize  = 12.sp,
                                textAlign = TextAlign.Center,
                                color     = if (owner == selectedOwner) Color.Blue else Color.Gray
                            )
                        }
                    }
                }

            }


        }

        if (showPopup) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000)) // 반투명 배경
                    .clickable { showPopup = false } // 바깥 누르면 닫힘
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.White)
                        .padding(16.dp)
                        .width(280.dp)
                        .heightIn(max = 400.dp)
                ) {
                    LazyColumn {
                        items(joinedRoutines) { routine ->
                            Text(
                                text = routine.name,
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedRoutine = routine
                                        showPopup = false
                                    }
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }



        }




    }
}


fun loadJoinedRoutinesFromAssets(context: Context): List<RoutineItem> {
    val json = context.assets.open("routines.json").bufferedReader().use { it.readText() }
    val type = object : TypeToken<List<RoutineItem>>() {}.type
    return Gson().fromJson<List<RoutineItem>>(json, type)
        .filter { it.isJoined }
}

@Composable
fun AnimatedHighlighter(text: String) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedWidth by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "highlight_width"
    )

    LaunchedEffect(text) {
        animationPlayed = false
        delay(800) // 짧게 딜레이 줘서 초기화 느낌
        animationPlayed = true
    }

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .padding(bottom = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.zIndex(1f)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    clip = true
                }
                .drawBehind {
                    drawRect(
                        color = Color(0xFF99CCFF),
                        size = size.copy(width = size.width * animatedWidth, height = size.height * 0.6f),
                        topLeft = Offset.Zero
                    )
                }
        )




    }
}

fun loadJoinedRoutinesFromInternalStorage(context: Context): List<RoutineItem> {
    val file = File(context.filesDir, "routines.json")
    if (!file.exists()) return emptyList()

    val json = file.readText()
    val type = object : TypeToken<List<RoutineItem>>() {}.type
    return Gson().fromJson<List<RoutineItem>>(json, type)
        .filter { it.isJoined }
}






