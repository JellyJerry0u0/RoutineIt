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
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import com.example.helloworld.ui.theme.Paperlogy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.LinearProgressIndicator
import org.threeten.bp.YearMonth


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

    // 캘린더에서 보고 있는 월 상태 관리
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // 진행률 계산을 위한 derived state
    val monthlyProgress by remember(selectedRoutine, allEntries, currentMonth, selectedOwner) {
        derivedStateOf {
            if (selectedRoutine == null) 0f else {
                calculateMonthlyProgress(selectedRoutine!!, allEntries, currentMonth, selectedOwner)
            }
        }
    }
    val streak by remember(selectedRoutine, allEntries) {
        derivedStateOf {
            if (selectedRoutine == null) 0 else calculateStreak(selectedRoutine!!, allEntries)
        }
    }

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
                .padding(horizontal = 16.dp, vertical = 0.dp) // 상단 여백 최소화
        ) {
            Spacer(Modifier.height(20.dp)) // 루틴 선택 탭을 더 아래로
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center, // 중앙 정렬!
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoutinePill(
                    routineName = selectedRoutine?.name ?: "",
                    isDone = isRoutineDoneToday(selectedRoutine, allEntries),
                    streak = streak,
                    onClick = { showPopup = true }
                )
            }
            
            // 진행률 바 추가
            Spacer(Modifier.height(12.dp))
            MonthlyProgressBar(progress = monthlyProgress, yearMonth = currentMonth)
            
            Spacer(Modifier.height(2.dp)) // 캘린더와의 간격을 줄임
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
                },
                currentMonth = currentMonth,
                onMonthChange = { newMonth -> currentMonth = newMonth }
            )





            if (selectedPopupImage != null) {
                val entry = selectedPopupImage!!
                val fileName = Uri.parse(entry.imageUri).lastPathSegment!!
                val imageFile = File(context.filesDir, "images/$fileName")
                val imageUrl = if (imageFile.exists()) imageFile else entry.imageUri
                val parsedDate = try { org.threeten.bp.LocalDate.parse(entry.date) } catch (e: Exception) { null }
                val formattedDate = parsedDate?.let { it.format(org.threeten.bp.format.DateTimeFormatter.ofPattern("yyyy.MM.dd")) } ?: entry.date

                AlertDialog(
                    onDismissRequest = { selectedPopupImage = null },
                    title = null,
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(24.dp))
                                .padding(0.dp)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    formattedDate,
                                    fontFamily = Paperlogy,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFF222222)
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "닫기",
                                    tint = Color(0xFF888888),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { selectedPopupImage = null }
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(horizontal = 24.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .padding(horizontal = 24.dp)
                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.TopStart // 좌상단 정렬
                            ) {
                                Text(
                                    entry.memo,
                                    fontFamily = Paperlogy,
                                    fontSize = 15.sp,
                                    color = Color(0xFF888888),
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .align(Alignment.TopStart), // 좌상단 정렬
                                    textAlign = TextAlign.Start // 중앙정렬 대신 좌정렬
                                )
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    },
                    confirmButton = {},
                    containerColor = Color.Transparent,
                    shape = RoundedCornerShape(24.dp)
                )
            }
            val today = LocalDate.now().toString()
            val myRoutineName = selectedRoutine?.name ?: "3대500클럽"

            // 1. 내 피드(실제 데이터)
            val myFeed = allEntries.find {
                it.date == today && it.routineName == myRoutineName && it.owner == "내 루틴"
            }

            // 2. 루틴별 더미 참가자 선정 (중복 허용)
            // 1. data class 선언을 먼저!
            data class DummyParticipant(val name: String, val image: String, val memo: String)

            // 2. 그 다음 리스트 생성
            val dummyParticipants = listOf(
                DummyParticipant("김철수", "https://randomuser.me/api/portraits/men/10.jpg", "오늘도 화이팅!"),
                DummyParticipant("이영희", "https://randomuser.me/api/portraits/women/20.jpg", "꾸준함이 답이다."),
                DummyParticipant("박민수", "https://randomuser.me/api/portraits/men/30.jpg", "운동 끝!"),
                DummyParticipant("홍길동", "https://randomuser.me/api/portraits/men/40.jpg", "새 루틴 도전!")
            )

            // 3. 현재 루틴의 더미 참가자
            val routineParticipants = remember(joinedRoutines) {
                // 각 루틴마다 2~3명 랜덤 배정
                joinedRoutines.associate { routine ->
                    val shuffled = dummyParticipants.shuffled()
                    val count = (2..3).random()
                    routine.name to shuffled.take(count)
                }
            }

            // 4. 더미 엔트리 생성
            val currentDummyParticipants = routineParticipants[myRoutineName] ?: emptyList()

            // 5. 피드 리스트: 내 피드(있으면) + 더미 피드
            val dummyEntries = currentDummyParticipants.map { participant ->
                RoutineEntry(
                    routineName = myRoutineName,
                    date = today,
                    imageUri = participant.image,
                    memo = participant.memo,
                    owner = participant.name
                )
            }

            // 6. 피드 리스트: 내 피드(있으면) + 더미 피드
            val feedEntries = listOfNotNull(myFeed) + dummyEntries

            if (feedEntries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "참여자 피드",
                    fontFamily = Paperlogy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF222222),
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(feedEntries) { entry ->
                        val isSelected = entry.owner == selectedOwner
                        val routineItem = joinedRoutines.find { it.name == entry.routineName } ?: selectedRoutine
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(120.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF90B4E6) else Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .clickable { selectedOwner = entry.owner }
                                .padding(12.dp)
                        ) {
                            AsyncImage(
                                model = entry.imageUri,
                                contentDescription = entry.owner,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    entry.owner,
                                    fontFamily = Paperlogy,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color(0xFF90B4E6) else Color(0xFF888888),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                // 모든 참여자에 대해 스트릭 표시 (entry.routineName 기준)
                                if (routineItem != null) {
                                    val ownerStreak = calculateStreak(routineItem, allEntries, entry.owner)
                                    if (ownerStreak > 0) {
                                        Spacer(Modifier.width(4.dp))
                                        Text("🌊$ownerStreak", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF2196F3))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                entry.memo,
                                fontFamily = Paperlogy,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = Color(0xFF888888),
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }
                }
            }


        }

        if (showPopup) {
            RoutineSelectPopup(
                routines = joinedRoutines,
                selectedRoutine = selectedRoutine,
                onSelect = { routine ->
                    selectedRoutine = routine
                    showPopup = false
                },
                onClose = { showPopup = false },
                allEntries = allEntries
            )
        }




    }
}

// 진행률 계산 함수 (yearMonth, owner 기준)
fun calculateMonthlyProgress(routine: RoutineItem, allEntries: List<RoutineEntry>, yearMonth: YearMonth, owner: String = "내 루틴"): Float {
    val routineDays = routine.days
    val startOfMonth = yearMonth.atDay(1)
    val daysInMonth = (1..yearMonth.lengthOfMonth()).map { day ->
        startOfMonth.withDayOfMonth(day)
    }
    val routineScheduledDays = daysInMonth.filter { date ->
        val dayOfWeek = when (date.dayOfWeek.value) {
            1 -> "월"
            2 -> "화"
            3 -> "수"
            4 -> "목"
            5 -> "금"
            6 -> "토"
            7 -> "일"
            else -> ""
        }
        routineDays.contains(dayOfWeek)
    }
    val myEntriesThisMonth = allEntries.filter {
        it.routineName == routine.name &&
        it.owner == owner &&
        LocalDate.parse(it.date).month == yearMonth.month &&
        LocalDate.parse(it.date).year == yearMonth.year
    }
    val completedScheduledDays = routineScheduledDays.count { date ->
        myEntriesThisMonth.any { it.date == date.toString() }
    }
    return if (routineScheduledDays.isNotEmpty()) {
        completedScheduledDays.toFloat() / routineScheduledDays.size
    } else 0f
}

// 스트릭 계산 함수
fun calculateStreak(routine: RoutineItem, allEntries: List<RoutineEntry>, owner: String = "내 루틴"): Int {
    val routineDays = routine.days
    val today = LocalDate.now()
    val startMonth = today.minusMonths(2).withDayOfMonth(1)
    val endMonth = today.withDayOfMonth(today.lengthOfMonth())
    val allScheduledDays = mutableListOf<LocalDate>()
    var date = startMonth
    while (!date.isAfter(endMonth)) {
        val dayOfWeek = when (date.dayOfWeek.value) {
            1 -> "월"
            2 -> "화"
            3 -> "수"
            4 -> "목"
            5 -> "금"
            6 -> "토"
            7 -> "일"
            else -> ""
        }
        if (routineDays.contains(dayOfWeek)) {
            allScheduledDays.add(date)
        }
        date = date.plusDays(1)
    }
    val entryDates = allEntries.filter {
        it.routineName == routine.name && it.owner == owner
    }.map { LocalDate.parse(it.date) }.toSet()
    var streak = 0
    for (scheduled in allScheduledDays.sortedDescending()) {
        if (scheduled.isAfter(today)) continue
        if (entryDates.contains(scheduled)) {
            streak++
        } else {
            break
        }
    }
    return streak
}

// 오늘 해당 루틴을 인증(완료)했는지 판별
fun isRoutineDoneToday(routine: RoutineItem?, allEntries: List<RoutineEntry>): Boolean {
    if (routine == null) return false
    val today = LocalDate.now().toString()
    return allEntries.any {
        it.routineName == routine.name && it.owner == "내 루틴" && it.date == today
    }
}

@Composable
fun MonthlyProgressBar(progress: Float, yearMonth: YearMonth) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${yearMonth.year}년 ${yearMonth.monthValue}월 진행률",
                fontFamily = Paperlogy,
                fontSize = 14.sp,
                color = Color(0xFF888888)
            )
            Text(
                "${(progress * 100).toInt()}%",
                fontFamily = Paperlogy,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF90B4E6)
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF90B4E6),
            trackColor = Color(0xFFF0F0F0)
        )
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

@Composable
fun RoutinePill(
    routineName: String,
    isDone: Boolean,
    streak: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFFF5F8FF), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .defaultMinSize(minHeight = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = routineName,
            fontFamily = Paperlogy,
            fontWeight = FontWeight.Bold, // match popup
            fontSize = 15.sp, // match popup
            color = Color(0xFF222222), // match popup
            maxLines = 1,
        )
        if (isDone) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(Color(0xFFA6C8FF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "완료",
                    fontFamily = Paperlogy,
                    fontWeight = FontWeight.Bold, // match popup
                    fontSize = 12.sp, // match popup
                    color = Color.White
                )
            }
        }
        // 스트릭 표시
        if (streak > 0) {
            Spacer(Modifier.width(8.dp))
            Text("🌊$streak", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF2196F3))
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Outlined.SwapHoriz,
            contentDescription = "루틴 교체",
            tint = Color(0xFF222A35),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun RoutineSelectPopup(
    routines: List<RoutineItem>,
    selectedRoutine: RoutineItem?,
    onSelect: (RoutineItem) -> Unit,
    onClose: () -> Unit,
    allEntries: List<RoutineEntry>
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x88000000))
            .clickable { onClose() }
    ) {
        Box(
            Modifier
                .align(Alignment.Center)
                .background(Color.White, RoundedCornerShape(18.dp))
                .padding(vertical = 18.dp, horizontal = 0.dp)
                .width(320.dp)
        ) {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "오늘의 루틴",
                        fontFamily = Paperlogy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF222222)
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = Color(0xFF888888),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onClose() }
                    )
                }
                Spacer(Modifier.height(16.dp))
                routines.forEach { routine ->
                    val isSelected = routine == selectedRoutine
                    val isDone = isRoutineDoneToday(routine, allEntries)
                    val streak = calculateStreak(routine, allEntries)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                if (isSelected) Color(0xFFF5F8FF) else Color(0xFFF7F8FA),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) Color(0xFF90B4E6) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelect(routine) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            routine.name,
                            fontFamily = Paperlogy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isSelected) Color.Black else Color(0xFF222222)
                        )
                        if (isDone) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                Modifier
                                    .background(Color(0xFFA6C8FF), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "완료",
                                    fontFamily = Paperlogy,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        if (streak > 0) {
                            Text("🌊$streak", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF2196F3))
                        }
                    }
                }
            }
        }
    }
}






