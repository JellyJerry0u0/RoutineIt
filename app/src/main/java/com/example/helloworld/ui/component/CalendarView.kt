package com.example.helloworld.ui.component

//import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helloworld.sampledata.RoutineImage
import coil.compose.AsyncImage
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.TextStyle
import java.util.Locale
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import com.example.helloworld.data.RoutineEntry
import androidx.core.net.toUri
import android.net.Uri
import android.util.Log
import androidx.compose.ui.layout.ContentScale
import androidx.core.net.toUri
import coil.request.ImageRequest
import java.io.File
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip

@Composable
fun CalendarView(
    selectedDate: LocalDate,
    selectedRoutine: com.example.helloworld.ui.screen.RoutineItem?,
    routineImages: List<RoutineEntry>,
    context: android.content.Context,
    onDateClick: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val startOfMonth = currentMonth.atDay(1)
    val endOfMonth = currentMonth.atEndOfMonth()
    val startDayOfWeek = startOfMonth.dayOfWeek.value % 7 // 일요일 기준 0
    val totalDays = endOfMonth.dayOfMonth

    val dates = buildList {
        repeat(startDayOfWeek) { add(null) }
        for (i in 1..totalDays) {
            add(LocalDate.of(currentMonth.year, currentMonth.month, i))
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 월 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Outlined.ChevronLeft,
                    contentDescription = "이전 달",
                    tint = Color(0xFFB0B0B0),
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = String.format("%04d.%02d", currentMonth.year, currentMonth.monthValue),
                fontSize = 20.sp,
                fontFamily = com.example.helloworld.ui.theme.Paperlogy,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Outlined.ChevronRight,
                    contentDescription = "다음 달",
                    tint = Color(0xFFB0B0B0),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // 요일 표시
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach {
                Text(
                    it,
                    fontSize = 14.sp,
                    fontFamily = com.example.helloworld.ui.theme.Paperlogy,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB0B0B0),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // 날짜 그리드
        Column {
            dates.chunked(7).forEach { originalWeek ->
                val week = if (originalWeek.size < 7) {
                    originalWeek + List(7 - originalWeek.size) { null }
                } else originalWeek

                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clickable(enabled = date != null) { date?.let(onDateClick) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (date != null) {
                                val allEntriesForDate = routineImages.filter {
                                    LocalDate.parse(it.date) == date &&
                                            it.routineName == selectedRoutine?.name
                                }
                                val validEntry = allEntriesForDate.firstOrNull { entry ->
                                    val fileName = android.net.Uri.parse(entry.imageUri).lastPathSegment
                                    java.io.File(context.filesDir, "images/$fileName").exists()
                                }
                                val entryToShow = validEntry ?: allEntriesForDate.firstOrNull()

                                if (entryToShow != null) {
                                    val fileName = android.net.Uri.parse(entryToShow.imageUri).lastPathSegment
                                    val internalFile = java.io.File(context.filesDir, "images/$fileName")

                                    if (internalFile.exists()) {
                                        coil.compose.AsyncImage(
                                            model = internalFile,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(2.dp)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    } else {
                                        coil.compose.AsyncImage(
                                            model = coil.request.ImageRequest.Builder(context)
                                                .data(entryToShow.imageUri.toUri())
                                                .crossfade(true)
                                                .allowHardware(false)
                                                .build(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(2.dp)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = date.dayOfMonth.toString(),
                                            fontSize = 15.sp,
                                            fontFamily = com.example.helloworld.ui.theme.Paperlogy,
                                            fontWeight = FontWeight.Normal,
                                            color = Color(0xFF222222)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
