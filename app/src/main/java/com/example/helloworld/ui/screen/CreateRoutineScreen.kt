package com.example.helloworld.ui.screen

import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helloworld.ui.theme.Paperlogy
import java.util.Calendar
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close

@Composable
fun CreateRoutineScreen(
    onRoutineCreated: (Routine) -> Unit,
    onBack: () -> Unit,
    existingRoutines: List<Routine>
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var days by remember { mutableStateOf(setOf<String>()) }
    var showCustomTimePicker by remember { mutableStateOf(false) }
    var tempHour by remember { mutableStateOf(7) }
    var tempMinute by remember { mutableStateOf(0) }
    var time by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var amPm by remember { mutableStateOf(true) } // true: 오전, false: 오후

    val context = LocalContext.current
    val allDays = listOf("월", "화", "수", "목", "금", "토", "일")
    val categories = listOf("기상", "운동", "공부", "기타")
    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize()) {
        // 1) 배경 전용 레이어
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.White)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus()
                }
        )

        // 2) 실제 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 0.dp)
        ) {
            // 상단 바
            Spacer(Modifier.height(40.dp)) // 살짝만 더 아래로
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "루틴 만들기",
                    fontWeight = FontWeight.Black,
                    fontFamily = Paperlogy,
                    fontSize = 28.sp
                )
                Text(
                    text = "✕",
                    fontSize = 28.sp,
                    modifier = Modifier
                        .clickable { onBack() }
                        .padding(8.dp)
                )
            }
            Spacer(Modifier.height(16.dp)) // 간격 줄임

            // 이름 입력
            Text("루틴 이름", fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = Paperlogy)
            Spacer(Modifier.height(8.dp)) // 추가
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Paperlogy, fontSize = 16.sp),
                singleLine = true
            )
            Spacer(Modifier.height(18.dp))

            // 설명 입력
            Text("루틴 설명", fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = Paperlogy)
            Spacer(Modifier.height(8.dp)) // 추가
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                shape = RoundedCornerShape(12.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Paperlogy, fontSize = 16.sp),
                singleLine = false,
                maxLines = 4
            )
            Spacer(Modifier.height(18.dp))

            // 요일 선택
            Text("요일", fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = Paperlogy)
            Spacer(Modifier.height(8.dp)) // 기존 6dp에서 8dp로
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allDays.forEach { day ->
                    val selected = days.contains(day)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = if (selected) BorderStroke(2.dp, Color(0xFF90B4E6)) else BorderStroke(1.dp, Color(0xFFE0E0E0)), // 파란색 진하게
                        modifier = Modifier
                            .height(38.dp)
                            .width(38.dp)
                            .clickable {
                                days = if (selected) days - day else days + day
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                day,
                                fontFamily = Paperlogy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))

            // 시간 입력란 (커스텀)
            Text("시간", fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = Paperlogy)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
                    .clickable {
                        focusManager.clearFocus()
                        showCustomTimePicker = true
                    }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (time.isEmpty()) "시간을 선택하세요" else time,
                    style = LocalTextStyle.current.copy(fontSize = 16.sp),
                    color = if (time.isEmpty()) Color.Gray else Color.Black
                )
            }

            if (showCustomTimePicker) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0x88000000))
                        .clickable(
                            onClick = { showCustomTimePicker = false },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                ) {
                    Box(
                        Modifier
                            .align(Alignment.Center)
                            // ② 연회색 1dp 테두리
                            .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(18.dp))
                            // ③ 흰 배경
                            .background(Color.White)
                            // ④ 패딩은 그대로
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("시간 선택", fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = Paperlogy)
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "닫기",
                                    modifier = Modifier.clickable { showCustomTimePicker = false }
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 16.dp)) {
                                // 시 DrumPicker
                                DrumPicker(
                                    items = (1..12).toList(),
                                    selected = tempHour,
                                    onSelected = {
                                        tempHour = it
                                        val ampmText = if (amPm) "오전" else "오후"
                                        time = "$ampmText %02d:%02d".format(it, tempMinute)
                                    }
                                )
                                Text(":", fontSize = 32.sp, modifier = Modifier.padding(horizontal = 8.dp))
                                // 분 DrumPicker (더 오른쪽으로)
                                Spacer(Modifier.width(24.dp))
                                DrumPicker(
                                    items = (0..59).toList(),
                                    selected = tempMinute,
                                    onSelected = {
                                        tempMinute = it
                                        val ampmText = if (amPm) "오전" else "오후"
                                        time = "$ampmText %02d:%02d".format(tempHour, it)
                                    }
                                )
                            }
                            Spacer(Modifier.height(24.dp))
                            // 오전/오후 버튼 Row (중앙 하단, 넓고 둥글게, 그림자X)
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(50.dp)
                                        .border(
                                            width = 1.dp,
                                            color = if (amPm) Color(0xFF90B4E6) else Color(0xFFE0E0E0),
                                            shape = RoundedCornerShape(8.dp) // 더 사각지게
                                        )
                                        .background(
                                            color = if (amPm) Color(0xFF90B4E6) else Color(0xFFF3F3F3),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            amPm = true
                                            time = "오전 %02d:%02d".format(tempHour, tempMinute)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "오전",
                                        fontFamily = Paperlogy,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = if (amPm) Color.White else Color(0xFF888888)
                                    )
                                }
                                Spacer(Modifier.width(24.dp))
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(50.dp)
                                        .border(
                                            width = 1.dp,
                                            color = if (!amPm) Color(0xFF90B4E6) else Color(0xFFE0E0E0),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .background(
                                            color = if (!amPm) Color(0xFF90B4E6) else Color(0xFFF3F3F3),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            amPm = false
                                            time = "오후 %02d:%02d".format(tempHour, tempMinute)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "오후",
                                        fontFamily = Paperlogy,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = if (!amPm) Color.White else Color(0xFF888888)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))

            // 카테고리 선택
            Text("분류", fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = Paperlogy)
            Spacer(Modifier.height(8.dp)) // 기존 6dp에서 8dp로
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEachIndexed { idx, cat ->
                    val selected = category == cat
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) Color.White else Color(0xFFF3F3F3),
                        border = if (selected) BorderStroke(2.dp, Color(0xFF90B4E6)) else null,
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { category = cat }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = when (cat) {
                                    "기상" -> "☀️  기상"
                                    "운동" -> "🔥  운동"
                                    "공부" -> "✏️  공부"
                                    "기타" -> "⋯  기타"
                                    else -> cat
                                },
                                fontFamily = Paperlogy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (selected) Color.Black else Color(0xFF888888),
                                overflow = TextOverflow.Clip
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            // 안내 메시지
            Text(
                text = "루틴은 모두가 포기할 때까지 끝나지 않습니다",
                color = Color(0xFFD32F2F),
                fontFamily = Paperlogy,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(18.dp))

            // 루틴 시작 버튼
            Button(
                onClick = {
                    when {
                        name.isBlank() || description.isBlank() || days.isEmpty() || time.isBlank() || category.isBlank() -> {
                            error = "모든 항목을 입력해주세요."
                        }
                        existingRoutines.any { it.name == name } -> {
                            error = "이미 존재하는 루틴 이름입니다."
                        }
                        else -> {
                            onRoutineCreated(
                                Routine(
                                    name = name,
                                    description = description,
                                    days = days.toList(),
                                    time = time,
                                    participants = 1,
                                    category = category,
                                    isJoined = true
                                )
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90B4E6)) // 파란색 진하게
            ) {
                Text(
                    text = "루틴 시작!",
                    color = Color.White,
                    fontFamily = Paperlogy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = Color.Red, fontSize = 13.sp, fontFamily = Paperlogy, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun DrumPicker(
    items: List<Int>,
    selected: Int,
    onSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState(items.indexOf(selected))
    LaunchedEffect(selected) {
        listState.animateScrollToItem(items.indexOf(selected))
    }
    Box(
        Modifier
            .height(120.dp)
            .width(70.dp)
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { idx ->
                val value = items[idx]
                Text(
                    "%02d".format(value),
                    fontSize = if (value == selected) 32.sp else 20.sp,
                    fontWeight = if (value == selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (value == selected) Color.Black else Color(0xFFBBBBBB),
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        // 클릭 핸들만 잡고 나머지 드래그는 그대로 흘려보냄
                        .pointerInput(Unit) {
                            detectTapGestures {
                                onSelected(value)
                            }
                        }
                )
            }
        }
    }
}