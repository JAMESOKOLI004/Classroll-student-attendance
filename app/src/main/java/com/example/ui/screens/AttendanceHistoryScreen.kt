package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DateUtils
import com.example.ui.ClassRollViewModel
import com.example.ui.Screen
import com.example.ui.components.AttendanceRateBadge
import com.example.ui.components.StatusPillBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.BackgroundSlate
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueSubtle
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusLateAmber
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHistoryScreen(
    initialClassId: Long?,
    viewModel: ClassRollViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.popBack()
    }

    LaunchedEffect(initialClassId) {
        viewModel.loadHistory(initialClassId)
    }

    val classes by viewModel.classesWithStats.collectAsState()
    val selectedClassId by viewModel.historyClassId.collectAsState()
    val currentYear by viewModel.historyYear.collectAsState()
    val currentMonth by viewModel.historyMonth.collectAsState()
    val selectedDate by viewModel.historySelectedDate.collectAsState()
    val monthRates by viewModel.historyMonthRates.collectAsState()
    val daySummary by viewModel.historyDaySummary.collectAsState()

    var isClassDropdownExpanded by remember { mutableStateOf(false) }

    val selectedClass = classes.find { it.classEntity.id == selectedClassId }?.classEntity

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundSlate,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Attendance History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.popBack() },
                        modifier = Modifier.testTag("history_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Class Filter Dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = isClassDropdownExpanded,
                    onExpandedChange = { isClassDropdownExpanded = !isClassDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedClass?.name ?: "All Classes (School-wide)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filter by Class") },
                        leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, tint = PrimaryBlue) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isClassDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isClassDropdownExpanded,
                        onDismissRequest = { isClassDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Classes (School-wide)") },
                            onClick = {
                                viewModel.setHistoryClass(null)
                                isClassDropdownExpanded = false
                            }
                        )
                        classes.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.classEntity.name} (${c.classEntity.room})") },
                                onClick = {
                                    viewModel.setHistoryClass(c.classEntity.id)
                                    isClassDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Month Selector & Calendar Card (12px rounded)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Month Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.changeHistoryMonth(-1) }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = TextPrimary)
                            }
                            Text(
                                text = DateUtils.formatMonthYear(currentYear, currentMonth),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            IconButton(onClick = { viewModel.changeHistoryMonth(1) }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Days of Week Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { dayLabel ->
                                Text(
                                    text = dayLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calendar Grid
                        CalendarMonthGrid(
                            year = currentYear,
                            monthIndex = currentMonth,
                            selectedDate = selectedDate,
                            monthRates = monthRates,
                            onDateSelected = { viewModel.selectHistoryDate(it) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Color Dots Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendDot(color = StatusPresentGreen, label = "≥90%")
                            Spacer(modifier = Modifier.width(14.dp))
                            LegendDot(color = StatusLateAmber, label = "75-89%")
                            Spacer(modifier = Modifier.width(14.dp))
                            LegendDot(color = StatusAbsentRed, label = "<75%")
                        }
                    }
                }
            }

            // Day's Record Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Record for ${DateUtils.formatIsoToShort(selectedDate)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        if (selectedDate == DateUtils.todayIso()) {
                            Text(
                                text = "Today",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue
                            )
                        }
                    }

                    if (selectedClassId != null && selectedClassId!! > 0) {
                        Button(
                            onClick = {
                                viewModel.loadTakeAttendance(selectedClassId!!, selectedDate)
                                viewModel.navigateTo(Screen.TakeAttendance(selectedClassId!!, selectedDate))
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Roll", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Day Summary Content
            val summary = daySummary
            if (summary == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Attendance Record for this Date",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            if (classes.isNotEmpty()) {
                                val targetCId = selectedClassId ?: classes.first().classEntity.id
                                Button(
                                    onClick = {
                                        viewModel.loadTakeAttendance(targetCId, selectedDate)
                                        viewModel.navigateTo(Screen.TakeAttendance(targetCId, selectedDate))
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Text("Take Attendance for this Day")
                                }
                            }
                        }
                    }
                }
            } else {
                // Summary Stat Bar
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format(Locale.US, "%.0f%%", summary.attendancePercentage),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = if (summary.attendancePercentage >= 90f) StatusPresentGreen else StatusLateAmber
                                )
                                Text("Attendance", fontSize = 11.sp, color = TextMuted)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${summary.presentCount}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StatusPresentGreen)
                                Text("Present", fontSize = 11.sp, color = TextMuted)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${summary.lateCount}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StatusLateAmber)
                                Text("Late", fontSize = 11.sp, color = TextMuted)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${summary.absentCount}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StatusAbsentRed)
                                Text("Absent", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }

                // Student Breakdown List for Selected Day
                items(summary.studentRecords, key = { it.student.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StudentAvatar(
                                initials = item.student.initials,
                                colorHex = item.student.avatarColorHex,
                                size = 38.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.student.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                if (item.remark.isNotBlank()) {
                                    Text(
                                        text = item.remark,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                } else {
                                    Text(
                                        text = item.student.studentId,
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                            StatusPillBadge(status = item.status)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun CalendarMonthGrid(
    year: Int,
    monthIndex: Int,
    selectedDate: String,
    monthRates: Map<String, Float>,
    onDateSelected: (String) -> Unit
) {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, monthIndex)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun, 1=Mon...
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val isoFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (week in 0 until totalCells / 7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (dayCol in 0..6) {
                    val cellIndex = week * 7 + dayCol
                    val dayNum = cellIndex - firstDayOfWeek + 1

                    if (dayNum in 1..daysInMonth) {
                        cal.set(Calendar.DAY_OF_MONTH, dayNum)
                        val dateIso = isoFormatter.format(cal.time)
                        val isSelected = dateIso == selectedDate
                        val rate = monthRates[dateIso]

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryBlueSubtle else Color.Transparent)
                                .then(
                                    if (isSelected) Modifier.border(1.5.dp, PrimaryBlue, RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                                .clickable { onDateSelected(dateIso) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$dayNum",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) PrimaryBlue else TextPrimary
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                // Color dot indicator
                                if (rate != null) {
                                    val dotColor = when {
                                        rate >= 90f -> StatusPresentGreen
                                        rate >= 75f -> StatusLateAmber
                                        else -> StatusAbsentRed
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(5.dp))
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = TextMuted)
    }
}
