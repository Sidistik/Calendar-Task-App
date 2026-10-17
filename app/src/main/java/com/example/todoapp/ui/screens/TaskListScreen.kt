package com.example.finalDueDateCalendarApp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.finalDueDateCalendarApp.data.local.CalendarTaskEntity
import com.example.finalDueDateCalendarApp.viewmodel.AuthViewModel
import com.example.finalDueDateCalendarApp.viewmodel.CalendarTaskViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTaskListScreen(
    authViewModel: AuthViewModel,
    taskViewModel: CalendarTaskViewModel,
    onAddTask: () -> Unit,
    onEditTask: (Int) -> Unit,
    onLoggedOut: () -> Unit
) {
    val authState by authViewModel.currentUserId.collectAsState()
    val state by taskViewModel.uiState.collectAsState()

    LaunchedEffect(authState) {
        authState?.let { taskViewModel.startObservingTasks(it) }
    }

    val selectedDateTasks = state.tasks
        .filter { CalendarTaskViewModel.millisToDate(it.dueDateMillis) == state.selectedDate }
        .sortedBy { it.isCompleted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Due Date Calendar") },
                actions = {
                    TextButton(onClick = {
                        authViewModel.logout()
                        onLoggedOut()
                    }) {
                        Text("Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                taskViewModel.prepareNewTaskForSelectedDate()
                onAddTask()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add task to selected date")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalendarGrid(
                visibleMonth = state.visibleMonth,
                selectedDate = state.selectedDate,
                tasks = state.tasks,
                onPreviousMonth = taskViewModel::previousMonth,
                onNextMonth = taskViewModel::nextMonth,
                onDateSelected = taskViewModel::selectDate
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tasks for ${state.selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${selectedDateTasks.size} item(s) on this date",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                FilledTonalButton(onClick = {
                    taskViewModel.prepareNewTaskForSelectedDate()
                    onAddTask()
                }) {
                    Text("Add")
                }
            }

            if (selectedDateTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks on this date. Select a date and tap Add.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(selectedDateTasks, key = { it.id }) { task ->
                        CalendarTaskCard(
                            task = task,
                            onToggleComplete = { taskViewModel.toggleTaskCompletion(task) },
                            onEdit = { onEditTask(task.id) },
                            onDelete = { taskViewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    tasks: List<CalendarTaskEntity>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val monthTitle = visibleMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    val days = buildCalendarDays(visibleMonth)
    val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onPreviousMonth) { Text("<") }
                Text(monthTitle, style = MaterialTheme.typography.titleLarge)
                OutlinedButton(onClick = onNextMonth) { Text(">") }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                weekdays.forEach {
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(300.dp),
                userScrollEnabled = false
            ) {
                items(days) { date ->
                    val isCurrentMonth = YearMonth.from(date) == visibleMonth
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()
                    val count = tasks.count { CalendarTaskViewModel.millisToDate(it.dueDateMillis) == date }

                    CalendarDayCell(
                        date = date,
                        isCurrentMonth = isCurrentMonth,
                        isSelected = isSelected,
                        isToday = isToday,
                        taskCount = count,
                        onClick = { onDateSelected(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    taskCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(3.dp)
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isToday -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCurrentMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
            )
            if (taskCount > 0) {
                Text(
                    text = "$taskCount task",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun buildCalendarDays(month: YearMonth): List<LocalDate> {
    val firstDay = month.atDay(1)
    val startOffset = (firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val startDate = firstDay.minusDays(startOffset.toLong())
    return (0 until 42).map { startDate.plusDays(it.toLong()) }
}

@Composable
private fun CalendarTaskCard(
    task: CalendarTaskEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dueDate = CalendarTaskViewModel.millisToDate(task.dueDateMillis)
    val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), dueDate)
    val countdownText = when {
        task.isCompleted -> "Completed"
        daysLeft > 1 -> "$daysLeft days left"
        daysLeft == 1L -> "1 day left"
        daysLeft == 0L -> "Due today"
        else -> "${-daysLeft} day(s) overdue"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleComplete() })

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    if (task.description.isNotBlank()) {
                        Text(task.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit task")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete task")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(countdownText) })
                SuggestionChip(onClick = {}, label = { Text(task.priority) })
            }
        }
    }
}
