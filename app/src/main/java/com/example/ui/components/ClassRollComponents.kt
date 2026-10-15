package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.TrendPoint
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueSubtle
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusAbsentRedDark
import com.example.ui.theme.StatusAbsentRedSubtle
import com.example.ui.theme.StatusLateAmber
import com.example.ui.theme.StatusLateAmberDark
import com.example.ui.theme.StatusLateAmberSubtle
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.StatusPresentGreenDark
import com.example.ui.theme.StatusPresentGreenSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun StudentAvatar(
    initials: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val bgColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (_: Exception) {
        PrimaryBlue
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = if (size > 48.dp) 20.sp else 14.sp
        )
    }
}

@Composable
fun AttendanceRateBadge(
    rate: Float?,
    modifier: Modifier = Modifier
) {
    if (rate == null) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = StatusLateAmberSubtle,
            modifier = modifier
        ) {
            Text(
                text = "Needs Roll",
                color = StatusLateAmberDark,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        return
    }

    val (bgColor, textColor) = when {
        rate >= 90f -> Pair(StatusPresentGreenSubtle, StatusPresentGreenDark)
        rate >= 75f -> Pair(StatusLateAmberSubtle, StatusLateAmberDark)
        else -> Pair(StatusAbsentRedSubtle, StatusAbsentRedDark)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = String.format(Locale.US, "%.0f%% Rate", rate),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StatusPillBadge(
    status: AttendanceStatus,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = status.containerColor,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(status.color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status.displayName,
                color = status.darkColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun AttendanceSegmentedControl(
    selectedStatus: AttendanceStatus,
    onStatusSelected: (AttendanceStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF1F5F9))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        AttendanceSegmentButton(
            status = AttendanceStatus.PRESENT,
            isSelected = selectedStatus == AttendanceStatus.PRESENT,
            onClick = { onStatusSelected(AttendanceStatus.PRESENT) },
            modifier = Modifier.weight(1f)
        )
        AttendanceSegmentButton(
            status = AttendanceStatus.LATE,
            isSelected = selectedStatus == AttendanceStatus.LATE,
            onClick = { onStatusSelected(AttendanceStatus.LATE) },
            modifier = Modifier.weight(1f)
        )
        AttendanceSegmentButton(
            status = AttendanceStatus.ABSENT,
            isSelected = selectedStatus == AttendanceStatus.ABSENT,
            onClick = { onStatusSelected(AttendanceStatus.ABSENT) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AttendanceSegmentButton(
    status: AttendanceStatus,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) status.color else Color.Transparent
    val contentColor = if (isSelected) Color.White else TextSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (status) {
                    AttendanceStatus.PRESENT -> Icons.Default.Check
                    AttendanceStatus.LATE -> Icons.Default.Schedule
                    AttendanceStatus.ABSENT -> Icons.Default.Close
                },
                contentDescription = status.displayName,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.displayName,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun CircularAttendanceChart(
    percentage: Float,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 14.dp
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(percentage) {
        animatedProgress.animateTo(
            targetValue = (percentage / 100f).coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000)
        )
    }

    val chartColor = when {
        percentage >= 90f -> StatusPresentGreen
        percentage >= 75f -> StatusLateAmber
        else -> StatusAbsentRed
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val diameter = this.size.minDimension - stroke
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arcSize = Size(diameter, diameter)

            // Background track
            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                color = chartColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format(Locale.US, "%.1f%%", percentage),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                color = TextPrimary
            )
            Text(
                text = when {
                    percentage >= 95f -> "Excellent"
                    percentage >= 90f -> "Great"
                    percentage >= 75f -> "Average"
                    else -> "At Risk"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = chartColor
            )
        }
    }
}

@Composable
fun AttendanceTrendLineChart(
    trendPoints: List<TrendPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = PrimaryBlue
) {
    if (trendPoints.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(SurfaceWhite, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No trend data available yet", color = TextMuted, fontSize = 13.sp)
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attendance Trend",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Weekly Rolling Average",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val width = size.width
                val height = size.height - 30.dp.toPx() // Leave space for labels
                val points = trendPoints
                val count = points.size

                val minVal = 50f
                val maxVal = 100f

                val xStep = if (count > 1) width / (count - 1) else width / 2

                val coordinates = points.mapIndexed { index, point ->
                    val x = if (count > 1) index * xStep else width / 2
                    val normalizedY = ((point.percentage - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                    val y = height - (normalizedY * height)
                    Offset(x, y)
                }

                // Draw horizontal guide lines
                val gridY75 = height - (((75f - minVal) / (maxVal - minVal)) * height)
                val gridY100 = height - (((100f - minVal) / (maxVal - minVal)) * height)
                drawLine(
                    color = Color(0xFFF1F5F9),
                    start = Offset(0f, gridY75),
                    end = Offset(width, gridY75),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color(0xFFF1F5F9),
                    start = Offset(0f, gridY100),
                    end = Offset(width, gridY100),
                    strokeWidth = 1.dp.toPx()
                )

                // Fill gradient path under curve
                if (coordinates.size > 1) {
                    val fillPath = Path().apply {
                        moveTo(coordinates.first().x, height)
                        coordinates.forEach { lineTo(it.x, it.y) }
                        lineTo(coordinates.last().x, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                            startY = 0f,
                            endY = height
                        )
                    )
                }

                // Draw line
                for (i in 0 until coordinates.size - 1) {
                    drawLine(
                        color = lineColor,
                        start = coordinates[i],
                        end = coordinates[i + 1],
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Draw dots & labels
                coordinates.forEachIndexed { i, offset ->
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 3.5.dp.toPx(),
                        center = offset
                    )
                }
            }

            // X-axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trendPoints.forEach { point ->
                    Text(
                        text = point.label,
                        fontSize = 11.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
