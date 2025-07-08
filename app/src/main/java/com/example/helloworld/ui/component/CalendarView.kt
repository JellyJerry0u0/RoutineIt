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
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "이전 달")
            }
            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.KOREAN)} ${currentMonth.year}",
                fontSize = 20.sp
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "다음 달")
            }
        }

        // 요일 표시
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach {
                Text(it, fontSize = 14.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }

        // 날짜 그리드
        Column {
            dates.chunked(7).forEach { originalWeek ->
                // ✅ 항상 7개의 셀을 만들기 위해 null로 채움
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
                                .clickable(enabled = date != null) { date?.let(onDateClick) }
                        ) {
                            if (date != null) {
                                // 1) 해당 날짜의 모든 entry 를 모읍니다.
                                val allEntriesForDate = routineImages.filter {
                                    LocalDate.parse(it.date) == date &&
                                            it.routineName == selectedRoutine?.name
                                }

                                // 2) internal/images 폴더에 파일이 실제로 존재하는 entry 를 먼저 선택
                                val validEntry = allEntriesForDate.firstOrNull { entry ->
                                    // entry.imageUri 가 "content://.../images/파일명" 이므로
                                    val fileName = Uri.parse(entry.imageUri).lastPathSegment
                                    File(context.filesDir, "images/$fileName").exists()
                                }
                                // 3) 만약 internal 파일이 없는(legacy) 항목만 남았다면, 그냥 첫 항목을 선택
                                val entryToShow = validEntry ?: allEntriesForDate.firstOrNull()

                                if (entryToShow != null) {
                                    // 4) 이제 entryToShow 에 맞춰 로드합니다.
                                    val fileName = Uri.parse(entryToShow.imageUri).lastPathSegment
                                    val internalFile = File(context.filesDir, "images/$fileName")

                                    if (internalFile.exists()) {
                                        // internal 복사본이 있으면 파일로부터 읽기
                                        AsyncImage(
                                            model = internalFile,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(2.dp)
                                                .aspectRatio(1f),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        // 복사본이 없으면 직접 URI 로 읽기 (legacy camera URI 등)
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(entryToShow.imageUri.toUri())
                                                .crossfade(true)
                                                .allowHardware(false)
                                                .build(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(2.dp)
                                                .aspectRatio(1f),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else {
                                    // 5) 기록이 하나도 없으면 날짜 숫자 표시
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = date.dayOfMonth.toString(), fontSize = 13.sp)
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
