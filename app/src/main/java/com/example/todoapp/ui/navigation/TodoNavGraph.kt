package com.example.finalDueDateCalendarApp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.finalDueDateCalendarApp.data.repository.AuthRepository
import com.example.finalDueDateCalendarApp.data.repository.CalendarTaskRepository
import com.example.finalDueDateCalendarApp.ui.screens.AddEditCalendarTaskScreen
import com.example.finalDueDateCalendarApp.ui.screens.CalendarTaskListScreen
import com.example.finalDueDateCalendarApp.ui.screens.LoginScreen
import com.example.finalDueDateCalendarApp.ui.screens.RegisterScreen
import com.example.finalDueDateCalendarApp.viewmodel.AuthViewModel
import com.example.finalDueDateCalendarApp.viewmodel.AuthViewModelFactory
import com.example.finalDueDateCalendarApp.viewmodel.CalendarTaskViewModel
import com.example.finalDueDateCalendarApp.viewmodel.CalendarTaskViewModelFactory

@Composable
fun CalendarTaskNavGraph(
    authRepository: AuthRepository,
    calendarTaskRepository: CalendarTaskRepository
) {
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository))
    val taskViewModel: CalendarTaskViewModel = viewModel(factory = CalendarTaskViewModelFactory(calendarTaskRepository))

    val currentUserId by authViewModel.currentUserId.collectAsState()
    val startDestination = if (currentUserId == null) Screen.Login.route else Screen.CalendarTasks.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    taskViewModel.startObservingTasks(it)
                    navController.navigate(Screen.CalendarTasks.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onBackToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    taskViewModel.startObservingTasks(it)
                    navController.navigate(Screen.CalendarTasks.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CalendarTasks.route) {
            CalendarTaskListScreen(
                authViewModel = authViewModel,
                taskViewModel = taskViewModel,
                onAddTask = { navController.navigate(Screen.AddEditCalendarTask.createRoute()) },
                onEditTask = { taskId -> navController.navigate(Screen.AddEditCalendarTask.createRoute(taskId)) },
                onLoggedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.CalendarTasks.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.AddEditCalendarTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId")?.takeIf { it != -1 }
            AddEditCalendarTaskScreen(
                taskId = taskId,
                taskViewModel = taskViewModel,
                onSaveComplete = { navController.popBackStack() }
            )
        }
    }
}
