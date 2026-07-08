package com.agroSystem.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Reusing same premium dark colors for consistency
// Slate900, Slate800, Slate400, Emerald500, Emerald400, Cyan500, Cyan400, Amber500, MintLight are imported or defined here if they aren't globally accessible.
// Let's redefine locally to make the file self-contained and clean.
private val HomeSlate900 = Color(0xFF0F172A)
private val HomeSlate800 = Color(0xFF1E293B)
private val HomeSlate700 = Color(0xFF334155)
private val HomeSlate400 = Color(0xFF94A3B8)
private val HomeEmerald500 = Color(0xFF10B981)
private val HomeEmerald400 = Color(0xFF34D399)
private val HomeCyan400 = Color(0xFF22D3EE)
private val HomeAmber500 = Color(0xFFF59E0B)
private val HomeRed400 = Color(0xFFF87171)

data class Shipment(
    val id: String,
    val product: String,
    val route: String,
    val status: String,
    val statusColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onResetOnboarding: () -> Unit) {
    val shipments = remember {
        listOf(
            Shipment("TX-9021", "Tomat Organik", "Malang → Surabaya", "Dalam Perjalanan", HomeEmerald400),
            Shipment("TX-9022", "Sayur Bayam", "Batu → Jakarta", "Selesai", HomeCyan400),
            Shipment("TX-9023", "Buah Stroberi", "Bandung → Bekasi", "Tertunda", HomeRed400),
            Shipment("TX-9024", "Cabai Rawit", "Kediri → Semarang", "Dalam Perjalanan", HomeEmerald400),
            Shipment("TX-9025", "Kentang Granola", "Dieng → Jogja", "Selesai", HomeCyan400)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Smart Supply Chain",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "AgroSystem Dashboard",
                            color = HomeSlate400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                },
                actions = {
                    // Reset Button (Useful for demo & testing the onboarding slides again)
                    IconButton(onClick = onResetOnboarding) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(HomeSlate800),
                            contentAlignment = Alignment.Center
                        ) {
                            // Circular icon simulation
                            Canvas(modifier = Modifier.size(16.dp)) {
                                drawArc(
                                    color = HomeSlate400,
                                    startAngle = 0f,
                                    sweepAngle = 270f,
                                    useCenter = false,
                                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HomeSlate900,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = HomeSlate900
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Item 1: Welcome Header Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                WelcomeHeaderCard()
            }

            // Item 2: Quick Metrics Grid
            item {
                Column {
                    Text(
                        text = "Status Logistik Hari Ini",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Aktif",
                            value = "12 Unit",
                            description = "Pengiriman",
                            accentColor = HomeEmerald400,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Suhu",
                            value = "4.2 °C",
                            description = "Rata-rata Chiller",
                            accentColor = HomeCyan400,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Item 3: Live Chart Analytics
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(HomeSlate800)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Efisiensi Rantai Pasok",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tingkat kesegaran vs waktu transit (Minggu Ini)",
                        color = HomeSlate400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Draw Dashboard Analytics Chart
                    DashboardChart(modifier = Modifier.height(140.dp))
                }
            }

            // Item 4: Recent Shipments Header
            item {
                Text(
                    text = "Aktivitas Pengiriman Terbaru",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Items list: Shipments
            items(shipments) { shipment ->
                ShipmentRowItem(shipment = shipment)
            }

            // Add extra spacer at the bottom
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun WelcomeHeaderCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(HomeSlate800, HomeSlate900),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Halo, Ricky Prakusa!",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Semua sistem terpantau optimal. 0 peringatan kritis hari ini.",
                color = HomeSlate400,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Pulse beacon indicating system online status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(HomeEmerald400)
                )
                Text(
                    text = "Sistem Online & Terenkripsi",
                    color = HomeEmerald400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    description: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = HomeSlate800),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = HomeSlate400,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = HomeSlate400,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
        }
    }
}

@Composable
fun DashboardChart(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        val width = size.width
        val height = size.height

        // Y-axis guidelines
        val lines = 3
        for (i in 0..lines) {
            val y = height * (i.toFloat() / lines)
            drawLine(
                color = HomeSlate700.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 2f
            )
        }

        // Beautiful smooth Bezier curve representing logistics yield
        val points = listOf(
            Offset(width * 0.05f, height * 0.85f),
            Offset(width * 0.2f, height * 0.7f),
            Offset(width * 0.35f, height * 0.4f),
            Offset(width * 0.5f, height * 0.48f),
            Offset(width * 0.65f, height * 0.15f),
            Offset(width * 0.8f, height * 0.28f),
            Offset(width * 0.95f, height * 0.05f)
        )

        val strokePath = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                val p0 = points[i - 1]
                val p1 = points[i]
                val cpX = (p0.x + p1.x) / 2
                cubicTo(cpX, p0.y, cpX, p1.y, p1.x, p1.y)
            }
        }

        // Fill area below chart line with beautiful green-to-transparent gradient
        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(points.last().x, height)
            lineTo(points.first().x, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(HomeEmerald400.copy(alpha = 0.3f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Draw line stroke
        drawPath(
            path = strokePath,
            color = HomeEmerald400,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // Draw circles at data checkpoints
        points.forEachIndexed { index, point ->
            if (index % 2 == 0 || index == points.lastIndex) {
                drawCircle(
                    color = HomeEmerald400,
                    radius = 8f,
                    center = point
                )
                drawCircle(
                    color = HomeSlate800,
                    radius = 4f,
                    center = point
                )
            }
        }
    }
}

@Composable
fun ShipmentRowItem(shipment: Shipment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HomeSlate800)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logistics box icon representation
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(HomeSlate700),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(18.dp)) {
                // Outer box
                drawRoundRect(
                    color = HomeSlate400,
                    topLeft = Offset(0f, 0f),
                    size = size,
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(width = 2.5f)
                )
                // Center package line
                drawLine(
                    color = HomeSlate400,
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = 2f
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Shipment Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = shipment.product,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = shipment.id,
                    color = HomeSlate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = shipment.route,
                color = HomeSlate400,
                fontSize = 12.sp
            )
        }

        // Status Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(shipment.statusColor.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = shipment.status,
                color = shipment.statusColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
