package com.example.helloworld.ui.screen

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helloworld.R
import com.example.helloworld.ui.theme.Paperlogy
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun TitleScreen(onClick: () -> Unit) {
    var play by remember { mutableStateOf(false) }
    var hideLogo by remember { mutableStateOf(false) }
    var textWidthPx by remember { mutableStateOf(0) }

    // 1) 텍스트 하이라이트 진행률 (0 → 1)
    val progress by animateFloatAsState(
        targetValue = if (play) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    // 2) 로고 alpha 애니메이션 (1 → 0)
    val logoAlpha by animateFloatAsState(
        targetValue = if (!hideLogo) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
    )

    // 3) 로고 scale 애니메이션 (1 → 0)
    val scale by animateFloatAsState(
        targetValue = if (!hideLogo) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
    )

    // 4) 애니메이션 순서 제어
    LaunchedEffect(Unit) {
        play = true
    }
    LaunchedEffect(progress) {
        if (progress == 1f) {
            delay(500)
            hideLogo = true
        }
    }

    // 5) UI 레이아웃
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() },
        color = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box {
                // — 타이틀 텍스트 + 하이라이트
                Text(
                    text = "루틴잇",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = Paperlogy,
                    modifier = Modifier
                        .graphicsLayer { clip = true }
                        .drawBehind {
                            val totalH = size.height
                            val hlH = totalH * .8f
                            val yOff = (totalH - hlH) / 2f
                            drawRect(
                                color = Color(0x8069A6F7),
                                topLeft = Offset(0f, yOff),
                                size = Size(size.width * progress, hlH)
                            )
                        }
                        .onGloballyPositioned { textWidthPx = it.size.width }
                )

                // — 로고 위치 계산
                val density = LocalDensity.current
                val startPx = with(density) { (-32).dp.toPx().roundToInt() }
                val endPx = with(density) { (textWidthPx.toDp() + 3.dp).toPx().roundToInt() }

                // — 로고 이미지: 이동 + 축소 + 페이드아웃
                Image(
                    painter = painterResource(R.drawable.transparent_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .offset { IntOffset(lerp(startPx, endPx, progress), 0) }
                        .graphicsLayer {
                            alpha = logoAlpha
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }
        }
    }
}

private fun lerp(a: Int, b: Int, t: Float): Int =
    (a + (b - a) * t).roundToInt()
