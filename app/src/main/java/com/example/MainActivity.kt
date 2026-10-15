package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.ClassRollViewModel
import com.example.ui.Screen
import com.example.ui.screens.AddStudentScreen
import com.example.ui.screens.AttendanceHistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.StudentListScreen
import com.example.ui.screens.StudentProfileScreen
import com.example.ui.screens.TakeAttendanceScreen
import com.example.ui.theme.BackgroundSlate
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ClassRollViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundSlate
                ) {
                    ClassRollApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ClassRollApp(viewModel: ClassRollViewModel) {
    val screenStack by viewModel.screenStack.collectAsState()
    val currentScreen = screenStack.lastOrNull() ?: Screen.Login

    BackHandler(enabled = screenStack.size > 1) {
        viewModel.popBack()
    }

    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            is Screen.Login -> LoginScreen(viewModel = viewModel)
            is Screen.Home -> HomeScreen(viewModel = viewModel)
            is Screen.StudentList -> StudentListScreen(classId = screen.classId, viewModel = viewModel)
            is Screen.AddStudent -> AddStudentScreen(
                classId = screen.classId,
                editStudentId = screen.editStudentId,
                viewModel = viewModel
            )
            is Screen.TakeAttendance -> TakeAttendanceScreen(
                classId = screen.classId,
                viewModel = viewModel
            )
            is Screen.AttendanceHistory -> AttendanceHistoryScreen(
                initialClassId = screen.initialClassId,
                viewModel = viewModel
            )
            is Screen.StudentProfile -> StudentProfileScreen(
                studentId = screen.studentId,
                fromClassId = screen.fromClassId,
                viewModel = viewModel
            )
            is Screen.Reports -> ReportsScreen(
                initialClassId = screen.initialClassId,
                viewModel = viewModel
            )
        }
    }
}
