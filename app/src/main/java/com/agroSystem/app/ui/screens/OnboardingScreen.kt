package com.agroSystem.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Color constants for rich premium look
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate400 = Color(0xFF94A3B8)
val Emerald500 = Color(0xFF10B981)
val Emerald400 = Color(0xFF34D399)
val MintLight = Color(0xFFD1FAE5)
val Cyan500 = Color(0xFF06B6D4)
val Cyan400 = Color(0xFF22D3EE)
val Amber500 = Color(0xFFF59E0B)

data class OnboardingPage(
    val title: String,
    val description: String,
    val illustration: @Composable () -> Unit
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pages = remember {
        listOf(
            OnboardingPage(
                title = "Smart Tracking",
                description = "Monitor perjalanan komoditas pangan secara real-time dari lahan petani hingga ke tangan konsumen.",
                illustration = { SmartTrackingIllustration() }
            ),
            OnboardingPage(
                title = "Kontrol Kualitas",
                description = "Pastikan kesegaran dan standar kualitas terbaik dengan sensor pemantau suhu & kelembaban otomatis.",
                illustration = { QualityControlIllustration() }
            ),
            OnboardingPage(
                title = "Analisis Agrobisnis",
                description = "Optimalkan rantai pasok Anda dengan wawasan analitik bertenaga AI untuk meminimalkan limbah pangan.",
                illustration = { AgroAnalyticsIllustration() }
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        // Top Row: Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pagerState.currentPage < pages.size - 1) {
                TextButton(
                    onClick = onFinished,
                    colors = ButtonDefaults.textButtonColors(contentColor = Slate400)
                ) {
                    Text(
                        text = "Skip",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Center Content: Horizontal Pager for slides
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp)
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Illustration container with beautiful subtle background glow
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Slate800.copy(alpha = 0.8f), Slate900),
                                radius = 450f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    pages[page].illustration()
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Title
                Text(
                    text = pages[page].title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = pages[page].description,
                    color = Slate400,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        // Bottom Controls Container
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pager Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 8.dp,
                        label = "indicator_width"
                    )
                    val color = if (isSelected) Emerald400 else Slate800

                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button
            val isLastPage = pagerState.currentPage == pages.size - 1
            Button(
                onClick = {
                    if (isLastPage) {
                        onFinished()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald500,
                    contentColor = Slate900
                )
            ) {
                Text(
                    text = if (isLastPage) "Mulai Sekarang" else "Lanjut",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SmartTrackingIllustration() {
    Canvas(modifier = Modifier.size(200.dp)) {
        val width = size.width
        val height = size.height

        // Draw grid dots
        for (i in 0..4) {
            for (j in 0..4) {
                drawCircle(
                    color = Slate800.copy(alpha = 0.5f),
                    radius = 3f,
                    center = Offset(width * (0.1f + i * 0.2f), height * (0.1f + j * 0.2f))
                )
            }
        }

        // Draw path connecting logistics points
        val path = Path().apply {
            moveTo(width * 0.15f, height * 0.8f)
            cubicTo(
                width * 0.35f, height * 0.75f,
                width * 0.25f, height * 0.35f,
                width * 0.5f, height * 0.45f
            )
            cubicTo(
                width * 0.75f, height * 0.55f,
                width * 0.65f, height * 0.15f,
                width * 0.85f, height * 0.2f
            )
        }

        // Draw path line with emerald gradient
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(Emerald400, Cyan400),
                start = Offset(0f, height),
                end = Offset(width, 0f)
            ),
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // Draw checkpoints nodes
        val points = listOf(
            Offset(width * 0.15f, height * 0.8f),
            Offset(width * 0.5f, height * 0.45f),
            Offset(width * 0.85f, height * 0.2f)
        )

        points.forEachIndexed { index, point ->
            // Outer pulsing ring
            drawCircle(
                color = (if (index == 1) Cyan400 else Emerald400).copy(alpha = 0.2f),
                radius = 24f,
                center = point
            )
            // Middle ring
            drawCircle(
                color = (if (index == 1) Cyan400 else Emerald400).copy(alpha = 0.4f),
                radius = 16f,
                center = point
            )
            // Inner solid dot
            drawCircle(
                color = if (index == 1) Cyan400 else Emerald400,
                radius = 8f,
                center = point
            )
        }
    }
}

@Composable
fun QualityControlIllustration() {
    Canvas(modifier = Modifier.size(200.dp)) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2, height / 2)

        // Draw radar/sensor outer circle waves
        drawCircle(
            color = Emerald400.copy(alpha = 0.1f),
            radius = 90f,
            center = center,
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = Emerald400.copy(alpha = 0.2f),
            radius = 70f,
            center = center,
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = Emerald400.copy(alpha = 0.3f),
            radius = 50f,
            center = center,
            style = Stroke(width = 3f)
        )

        // Draw sensor sweeping line (representing laser scanning)
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Emerald400, Color.Transparent),
                start = center,
                end = Offset(center.x + 85f, center.y - 40f)
            ),
            start = center,
            end = Offset(center.x + 85f, center.y - 40f),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )

        // Draw central leaf shape representing organic agro product
        val leafPath = Path().apply {
            moveTo(center.x - 30f, center.y + 30f)
            // Left curve
            cubicTo(
                center.x - 60f, center.y - 10f,
                center.x - 30f, center.y - 50f,
                center.x, center.y - 45f
            )
            // Right curve
            cubicTo(
                center.x + 30f, center.y - 50f,
                center.x + 60f, center.y - 10f,
                center.x - 30f, center.y + 30f
            )
        }

        drawPath(
            path = leafPath,
            brush = Brush.verticalGradient(
                colors = listOf(Emerald400, Emerald500),
                startY = center.y - 50f,
                endY = center.y + 30f
            )
        )

        // Leaf stem line
        drawLine(
            color = MintLight,
            start = Offset(center.x - 30f, center.y + 30f),
            end = Offset(center.x - 10f, center.y - 15f),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        // Scanning crosshairs or stars
        drawCircle(
            color = Amber500,
            radius = 6f,
            center = Offset(center.x + 40f, center.y + 30f)
        )
        drawCircle(
            color = Amber500,
            radius = 4f,
            center = Offset(center.x - 50f, center.y - 35f)
        )
    }
}

@Composable
fun AgroAnalyticsIllustration() {
    Canvas(modifier = Modifier.size(200.dp)) {
        val width = size.width
        val height = size.height

        // Draw horizontal grid lines
        for (i in 1..3) {
            val y = height * (0.25f * i)
            drawLine(
                color = Slate800.copy(alpha = 0.5f),
                start = Offset(width * 0.1f, y),
                end = Offset(width * 0.9f, y),
                strokeWidth = 2f
            )
        }

        // Draw Bar chart with rounded tops
        val barData = listOf(0.4f, 0.65f, 0.5f, 0.85f, 0.7f)
        val barWidth = width * 0.08f
        val startX = width * 0.15f
        val gap = width * 0.13f

        barData.forEachIndexed { index, value ->
            val x = startX + index * gap
            val barHeight = height * 0.6f * value
            val y = height * 0.8f - barHeight

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Cyan400.copy(alpha = 0.8f),
                        Cyan500.copy(alpha = 0.2f)
                    )
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )
        }

        // Draw line graph overlay with emerald dots
        val linePoints = listOf(
            Offset(width * 0.19f, height * 0.65f),
            Offset(width * 0.32f, height * 0.45f),
            Offset(width * 0.45f, height * 0.52f),
            Offset(width * 0.58f, height * 0.25f),
            Offset(width * 0.71f, height * 0.35f)
        )

        val linePath = Path().apply {
            linePoints.forEachIndexed { index, point ->
                if (index == 0) moveTo(point.x, point.y)
                else lineTo(point.x, point.y)
            }
        }

        drawPath(
            path = linePath,
            color = Emerald400,
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )

        // Draw glowing nodes on the line chart
        linePoints.forEach { point ->
            drawCircle(
                color = Emerald400.copy(alpha = 0.3f),
                radius = 12f,
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = point
            )
        }
    }
}
