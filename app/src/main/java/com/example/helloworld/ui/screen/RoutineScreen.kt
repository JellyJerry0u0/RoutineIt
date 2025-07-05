package com.example.helloworld.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.example.helloworld.sampledata.RoutineRepository

// 1. 루틴 데이터 클래스
data class Routine(
    val name: String,
    val description: String,
    val days: List<String>,
    val time: String,
    val participants: Int,
    val category: String,
    val isJoined: Boolean // 추가
)

// 3. 루틴 리스트 UI
@Composable
fun RoutineList(
    routines: List<Routine>,
    modifier: Modifier = Modifier,
    onRoutineClick: (Routine) -> Unit,
    leaveOverlayRoutineName: String?,
    onLeaveConfirm: (String) -> Unit,
    onLeaveCancel: () -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(routines) { routine ->
            Box {
                RoutineItem(routine, onClick = { onRoutineClick(routine) })
                if (leaveOverlayRoutineName == routine.name) {
                    // 오버레이 UI
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color(0xAAFFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "정말로 포기하실 건가요???",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clickable { onLeaveConfirm(routine.name) }
                                ) {
                                    Text(
                                        text = "네.",
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF3F3F3),
                                    modifier = Modifier
                                        .clickable { onLeaveCancel() }
                                ) {
                                    Text(
                                        text = "아니요!",
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
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

@Composable
fun RoutineItem(routine: Routine, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F8F8),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    routine.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    routine.time,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                routine.description,
                fontSize = 12.sp,
                color = Color(0xFF888888),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            RoutineDaysRow(routine.days)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${routine.participants}명 참여 중",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
                Text(
                    routine.category,
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }
        }
    }
}

@Composable
fun RoutineDaysRow(activeDays: List<String>) {
    val allDays = listOf("월", "화", "수", "목", "금", "토", "일")
    Row {
        allDays.forEach { day ->
            val isActive = activeDays.contains(day)
            Text(
                text = day,
                fontSize = 13.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) Color.Black else Color(0xFFCCCCCC),
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}

@Composable
fun TagHeader(
    tags: List<String>,
    selectedTags: Set<String>,
    onTagToggled: (String) -> Unit
) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        tags.forEach { tag ->
            val isSelected = selectedTags.contains(tag)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = if (isSelected) BorderStroke(2.dp, Color(0xFFB6C6E6)) else null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onTagToggled(tag) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = when (tag) {
                            "기상" -> "☀️"
                            "운동" -> "🔥"
                            "공부" -> "✏️"
                            "기타" -> "💬"
                            else -> tag
                        },
                        fontSize = 15.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = tag,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.Black else Color(0xFF888888)
                    )
                }
            }
        }
    }
}

@Composable
fun RoutineTabBar(selectedTab: String, onTabSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        listOf("같이하기", "내 루틴").forEach { tab ->
            val isSelected = selectedTab == tab
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = if (isSelected) BorderStroke(2.dp, Color(0xFFB6C6E6)) else null,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .height(40.dp)
                    .width(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelected(tab) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = tab,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.Black else Color(0xFF888888)
                    )
                }
            }
        }
    }
}

// 4. RoutineScreen에서 리스트 출력
@Composable
fun RoutineScreen() {
    val context = LocalContext.current
    var routines by remember { mutableStateOf<List<Routine>>(emptyList()) }

    // 최초 1회만 JSON에서 데이터 불러오기
    LaunchedEffect(Unit) {
        RoutineRepository.copyRoutinesJsonToInternalIfNeeded(context)
        routines = RoutineRepository.loadRoutinesFromInternal(context)
    }

    val tagList = listOf("기상", "운동", "공부", "기타")
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var selectedTab by remember { mutableStateOf("같이하기") } // "같이하기" or "내 루틴"

    // 탭에 따라 참여 여부로 먼저 필터링
    val joinedFiltered = when (selectedTab) {
        "같이하기" -> routines.filter { !it.isJoined }
        "내 루틴" -> routines.filter { it.isJoined }
        else -> routines
    }
    // 태그로 한 번 더 필터링 (0개 선택이면 전체)
    val filteredRoutines = if (selectedTags.isEmpty()) joinedFiltered
        else joinedFiltered.filter { it.category in selectedTags }

    var popupRoutine by remember { mutableStateOf<Routine?>(null) }
    var popupType by remember { mutableStateOf<String?>(null) } // "join" or "leave"
    var leaveOverlayRoutineName by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 24.dp, top = 32.dp, end = 24.dp, bottom = 24.dp)
    ) {
        Text(
            text = "루틴 목록",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        RoutineTabBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        Spacer(modifier = Modifier.height(8.dp))
        TagHeader(
            tags = tagList,
            selectedTags = selectedTags,
            onTagToggled = { tag ->
                selectedTags = if (selectedTags.contains(tag)) {
                    selectedTags - tag
                } else {
                    selectedTags + tag
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        RoutineList(
            routines = filteredRoutines,
            modifier = Modifier.weight(1f),
            onRoutineClick = { routine ->
                if (routine.isJoined) {
                    leaveOverlayRoutineName = routine.name
                } else {
                    popupRoutine = routine
                    popupType = "join"
                }
            },
            leaveOverlayRoutineName = leaveOverlayRoutineName,
            onLeaveConfirm = { routineName ->
                routines = routines.map {
                    if (it.name == routineName) it.copy(isJoined = false) else it
                }
                RoutineRepository.saveRoutinesToInternal(context, routines)
                leaveOverlayRoutineName = null
            },
            onLeaveCancel = { leaveOverlayRoutineName = null }
        )
    }

    if (popupRoutine != null && popupType == "join") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88000000)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 루틴 상세 정보 (카드 스타일)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8F8F8), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                popupRoutine!!.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                popupRoutine!!.time,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            popupRoutine!!.description,
                            fontSize = 12.sp,
                            color = Color(0xFF888888),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        RoutineDaysRow(popupRoutine!!.days)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${popupRoutine!!.participants}명 참여 중",
                                fontSize = 12.sp,
                                color = Color(0xFF888888)
                            )
                            Text(
                                popupRoutine!!.category,
                                fontSize = 12.sp,
                                color = Color(0xFF888888)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "정말로 참여하시겠습니까?",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFB6C6E6),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable {
                                    routines = routines.map {
                                        if (it.name == popupRoutine!!.name) it.copy(isJoined = true) else it
                                    }
                                    RoutineRepository.saveRoutinesToInternal(context, routines)
                                    popupRoutine = null
                                    popupType = null
                                }
                        ) {
                            Text(
                                text = "네!",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF3F3F3),
                            modifier = Modifier
                                .clickable {
                                    popupRoutine = null
                                    popupType = null
                                }
                        ) {
                            Text(
                                text = "아니요...",
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
