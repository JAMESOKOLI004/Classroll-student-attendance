package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DateUtils
import com.example.data.model.StudentWithStats
import com.example.ui.ClassRollViewModel
import com.example.ui.Screen
import com.example.ui.components.AttendanceRateBadge
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.BackgroundSlate
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueSubtle
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusAbsentRedSubtle
import com.example.ui.theme.StatusLateAmber
import com.example.ui.theme.StatusLateAmberSubtle
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.StatusPresentGreenSubtle
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    initialClassId: Long?,
    viewModel: ClassRollViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.popBack()
    }

    LaunchedEffect(initialClassId) {
        viewModel.loadReports(initialClassId)
    }

    val context = LocalContext.current
    val classes by viewModel.classesWithStats.collectAsState()
    val selectedClassId by viewModel.reportClassId.collectAsState()
    val rangeFilter by viewModel.reportRangeFilter.collectAsState()
    val studentRows by viewModel.reportStudentRows.collectAsState()
    val averageRate by viewModel.reportAverageRate.collectAsState()
    val exportToast by viewModel.reportExportToast.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var isClassDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(exportToast) {
        if (exportToast != null) {
            snackbarHostState.showSnackbar(exportToast ?: "")
            viewModel.clearToast()
        }
    }

    val selectedClass = classes.find { it.classEntity.id == selectedClassId }?.classEntity

    val perfectCount = studentRows.count { it.attendanceRate >= 98f }
    val atRiskCount = studentRows.count { it.attendanceRate < 75f }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundSlate,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Attendance Reports",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.popBack() },
                        modifier = Modifier.testTag("reports_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.exportCsv(context) },
                        modifier = Modifier.testTag("quick_export_icon_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export Report",
                            tint = PrimaryBlue
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
            // Class Selector Dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = isClassDropdownExpanded,
                    onExpandedChange = { isClassDropdownExpanded = !isClassDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedClass?.name ?: "All Classes (School-wide)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selected Roster") },
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
                                viewModel.setReportClass(null)
                                isClassDropdownExpanded = false
                            }
                        )
                        classes.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.classEntity.name} (${c.classEntity.room})") },
                                onClick = {
                                    viewModel.setReportClass(c.classEntity.id)
                                    isClassDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Date Range Filter Pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("This Week", "This Month", "Last 30 Days", "Entire Term").forEach { range ->
                        val isSelected = rangeFilter == range
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setReportRange(range) },
                            label = { Text(range, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlueSubtle,
                                selectedLabelColor = PrimaryBlue,
                                containerColor = SurfaceWhite
                            )
                        )
                    }
                }
            }

            // Summary Metric Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReportStatCard(
                        title = "Avg Attendance",
                        value = String.format(Locale.US, "%.1f%%", averageRate),
                        color = if (averageRate >= 90f) StatusPresentGreen else StatusLateAmber,
                        bg = if (averageRate >= 90f) StatusPresentGreenSubtle else StatusLateAmberSubtle,
                        modifier = Modifier.weight(1f)
                    )
                    ReportStatCard(
                        title = "Perfect Roll",
                        value = "$perfectCount",
                        color = StatusPresentGreen,
                        bg = StatusPresentGreenSubtle,
                        modifier = Modifier.weight(1f)
                    )
                    ReportStatCard(
                        title = "At Risk (<75%)",
                        value = "$atRiskCount",
                        color = StatusAbsentRed,
                        bg = StatusAbsentRedSubtle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Export Actions Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Export Attendance Data",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Download or share formatted reports for school administration",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.exportCsv(context) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("export_csv_btn"),
                                shape = RoundedCornerShape(22.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export CSV", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "ClassRoll Attendance Report - ${selectedClass?.name ?: "All Classes"}"
                                        )
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "ClassRoll Summary:\nClass: ${selectedClass?.name ?: "All Classes"}\nRange: $rangeFilter\nAvg Attendance: ${String.format(Locale.US, "%.1f%%", averageRate)}\nTotal Students: ${studentRows.size}\nPerfect: $perfectCount\nAt-Risk: $atRiskCount\nGenerated: ${DateUtils.todayIso()}"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Summary / PDF"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("export_pdf_btn"),
                                shape = RoundedCornerShape(22.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share / PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Attendance Summary Table Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Class-wide Attendance Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${studentRows.size} Students",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // Summary Table / List of Cards (12px rounded)
            if (studentRows.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No student records found for the selected filter", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(studentRows, key = { it.student.id }) { row ->
                    StudentReportRowCard(
                        item = row,
                        onClick = {
                            viewModel.loadStudentProfile(row.student.id)
                            viewModel.navigateTo(Screen.StudentProfile(studentId = row.student.id))
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun ReportStatCard(
    title: String,
    value: String,
    color: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun StudentReportRowCard(
    item: StudentWithStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("report_row_${item.student.id}"),
        shape = RoundedCornerShape(12.dp), // 12px rounded
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudentAvatar(
                initials = item.student.initials,
                colorHex = item.student.avatarColorHex,
                size = 42.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.student.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${item.student.studentId} • ${item.totalDays} sessions",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${item.presentCount} Present", fontSize = 11.sp, color = StatusPresentGreen, fontWeight = FontWeight.SemiBold)
                    Text("${item.lateCount} Late", fontSize = 11.sp, color = StatusLateAmber, fontWeight = FontWeight.SemiBold)
                    Text("${item.absentCount} Absent", fontSize = 11.sp, color = StatusAbsentRed, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            AttendanceRateBadge(rate = item.attendanceRate)
        }
    }
}
