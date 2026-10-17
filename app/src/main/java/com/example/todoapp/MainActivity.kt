package com.example.finalDueDateCalendarApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.finalDueDateCalendarApp.data.local.AppDatabase
import com.example.finalDueDateCalendarApp.data.repository.AuthRepository
import com.example.finalDueDateCalendarApp.data.repository.CalendarTaskRepository
import com.example.finalDueDateCalendarApp.ui.navigation.CalendarTaskNavGraph
import com.example.finalDueDateCalendarApp.ui.theme.finalDueDateCalendarAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val authRepository = AuthRepository(applicationContext, database.userDao())
        val taskRepository = CalendarTaskRepository(database.calendarTaskDao())

        setContent {
            finalDueDateCalendarAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CalendarTaskNavGraph(
                        authRepository = authRepository,
                        calendarTaskRepository = taskRepository
                    )
                }
            }
        }
    }
}
