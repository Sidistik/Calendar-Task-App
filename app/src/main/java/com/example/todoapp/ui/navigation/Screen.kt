package com.example.finalDueDateCalendarApp.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object CalendarTasks : Screen("calendar_tasks")
    data object AddEditCalendarTask : Screen("add_edit_calendar_task?taskId={taskId}") {
        fun createRoute(taskId: Int? = null): String = "add_edit_calendar_task?taskId=${taskId ?: -1}"
    }
}
