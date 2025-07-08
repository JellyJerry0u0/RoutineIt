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
import androidx.compose.foundation.border
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.example.helloworld.sampledata.RoutineRepository
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.helloworld.ui.theme.Paperlogy
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.shape.CircleShape

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
                                fontSize = 18.sp,
                                fontFamily = Paperlogy
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF90B4E6),
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
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 우상단: 월화수목금토일 한 줄로, 활성화만 진하게
                Row {
                    val allDays = listOf("월", "화", "수", "목", "금", "토", "일")
                    allDays.forEach { day ->
                        val isActive = routine.days.contains(day)
                        Text(
                            text = day,
                            fontFamily = Paperlogy,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isActive) Color.Black else Color(0xFFCCCCCC),
                            modifier = Modifier.padding(end = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    routine.name,
                    fontFamily = Paperlogy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF222222),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    routine.time,
                    fontFamily = Paperlogy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF222222)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                routine.description,
                fontFamily = Paperlogy,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color(0xFF888888),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "${routine.participants}명 참여 중",
                    fontFamily = Paperlogy,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
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

// 1. 태그 탭 개선
@Composable
fun TagHeader(
    tags: List<String>,
    selectedTags: Set<String>,
    onTagToggled: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEachIndexed { idx, tag ->
            val isSelected = selectedTags.contains(tag)
            Surface(
                shape = RoundedCornerShape(20.dp), // 더 둥글게
                color = if (isSelected) Color.White else Color(0xFFF3F3F3),
                border = if (isSelected) BorderStroke(2.dp, Color(0xFF90B4E6)) else null,
                modifier = Modifier
                    .weight(1f)            // ← 남은 공간을 균등 분배
                    .height(32.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onTagToggled(tag) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                //    modifier = Modifier.padding(horizontal = 14.dp)
                ) {
                    Text(
                        text = when (tag) {
                            "기상" -> "☀️  기상"
                            "운동" -> "🔥  운동"
                            "공부" -> "✏️  공부"
                            "기타" -> "⋯  기타"
                            else -> tag
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Paperlogy,
                        color = if (isSelected) Color.Black else Color(0xFF888888),
                        overflow = TextOverflow.Clip,
                    //    modifier = Modifier.widthIn(min = 60.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun RoutineTabBar(selectedTab: String, onTabSelected: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        listOf("같이하기", "내 루틴").forEachIndexed { idx, tab ->
            val isSelected = selectedTab == tab
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color.White else Color(0xFFF3F3F3),
                border = if (isSelected) BorderStroke(2.dp, Color(0xFF90B4E6)) else null,
                modifier = Modifier
                    .padding(end = if (idx != 1) 12.dp else 0.dp)
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
                        fontWeight = FontWeight.Bold,
                        fontFamily = Paperlogy,
                        color = if (isSelected) Color.Black else Color(0xFF888888)
                    )
                }
            }
        }
    }
}

// 2. 플러스 버튼 개선 (라운딩 사각형, 그림자X, 연한 파란색)
@Composable
fun FloatingAddButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, Color.Black),
            modifier = Modifier
                .padding(end = 24.dp, bottom = 32.dp)
                .size(50.dp)
                .clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "루틴 추가",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

// 3. 팝업 카드 디자인 개선
@Composable
fun RoutinePopupCard(
    routine: Routine,
    onYes: () -> Unit,
    onNo: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier
            .padding(horizontal = 32.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // 상단: 루틴명, 시간
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    routine.name,
                    fontFamily = Paperlogy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF222222),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    routine.time,
                    fontFamily = Paperlogy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF222222)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            // 참여자수, 요일
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${routine.participants}명 참여 중",
                    fontFamily = Paperlogy,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFFBBBBBB)
                )
                Row {
                    val allDays = listOf("월", "화", "수", "목", "금", "토", "일")
                    allDays.forEach { day ->
                        val isActive = routine.days.contains(day)
                        Text(
                            text = day + " ",
                            fontFamily = Paperlogy,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (isActive) Color.Black else Color(0xFFCCCCCC)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            // 설명
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, Color(0xFFE0E0E0)), RoundedCornerShape(8.dp))
            ) {
                Text(
                    routine.description,
                    fontFamily = Paperlogy,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = Color(0xFF222222),
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            // 질문
            Text(
                text = "정말로 자신 있으신가요?",
                fontFamily = Paperlogy,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFFD32F2F),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(18.dp))
            // 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF90B4E6),
                    modifier = Modifier
                        .width(100.dp)
                        .height(44.dp)
                        .clickable { onYes() }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "네!",
                            color = Color.White,
                            fontFamily = Paperlogy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFBBBBBB)),
                    modifier = Modifier
                        .width(100.dp)
                        .height(44.dp)
                        .clickable { onNo() }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "아니요..",
                            color = Color.Black,
                            fontFamily = Paperlogy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                }
            }
        }
    }
}

// 4. RoutineScreen에서 리스트 출력
    
@Composable
fun RoutineScreen(
    routines: List<Routine>,
    onRoutinesChanged: (List<Routine>) -> Unit,
    onCreateRoutine: () -> Unit
) {
    val context = LocalContext.current
    var routinesState by remember { mutableStateOf<List<Routine>>(emptyList()) }

    // 최초 1회만 JSON에서 데이터 불러오기
    LaunchedEffect(Unit) {
        RoutineRepository.copyRoutinesJsonToInternalIfNeeded(context)
        routinesState = RoutineRepository.loadRoutinesFromInternal(context)
    }

    val tagList = listOf("기상", "운동", "공부", "기타")
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var selectedTab by remember { mutableStateOf("같이하기") } // "같이하기" or "내 루틴"

    // 탭에 따라 참여 여부로 먼저 필터링
    val joinedFiltered = when (selectedTab) {
        "같이하기" -> routinesState.filter { !it.isJoined }
        "내 루틴" -> routinesState.filter { it.isJoined }
        else -> routinesState
    }
    // 태그로 한 번 더 필터링 (0개 선택이면 전체)
    val filteredRoutines = if (selectedTags.isEmpty()) joinedFiltered
        else joinedFiltered.filter { it.category in selectedTags }

    var popupRoutine by remember { mutableStateOf<Routine?>(null) }
    var popupType by remember { mutableStateOf<String?>(null) } // "join" or "leave"
    var leaveOverlayRoutineName by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(start = 24.dp, top = 32.dp, end = 24.dp, bottom = 24.dp)
        ) {
            Text(
                text = "루틴 목록",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = Paperlogy
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
                    routinesState = routinesState.map {
                        if (it.name == routineName) it.copy(isJoined = false) else it
                    }
                    RoutineRepository.saveRoutinesToInternal(context, routinesState)
                    leaveOverlayRoutineName = null
                },
                onLeaveCancel = { leaveOverlayRoutineName = null }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        FloatingAddButton(onClick = onCreateRoutine)
    }

    if (popupRoutine != null && popupType == "join") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88000000))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        // 팝업 닫기
                        popupRoutine = null
                        popupType = null
                    })
                },
            contentAlignment = Alignment.Center
        ) {
            RoutinePopupCard(
                routine = popupRoutine!!,
                onYes = {
                    routinesState = routinesState.map {
                        if (it.name == popupRoutine!!.name) it.copy(isJoined = true) else it
                    }
                    RoutineRepository.saveRoutinesToInternal(context, routinesState)
                    popupRoutine = null
                    popupType = null
                },
                onNo = {
                    popupRoutine = null
                    popupType = null
                }
            )
        }
    }
}
