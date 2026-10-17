package com.example.finalDueDateCalendarApp.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.finalDueDateCalendarApp.viewmodel.CalendarTaskViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCalendarTaskScreen(
    taskId: Int?,
    taskViewModel: CalendarTaskViewModel,
    onSaveComplete: () -> Unit
) {
    val state by taskViewModel.uiState.collectAsState()
    val priorities = listOf("Low", "Medium", "High")
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    LaunchedEffect(taskId) {
        taskViewModel.loadTask(taskId)
    }

    val datePickerDialog = remember(state.selectedDate) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                taskViewModel.updateSelectedDate(LocalDate.of(year, month + 1, dayOfMonth))
            },
            state.selectedDate.year,
            state.selectedDate.monthValue - 1,
            state.selectedDate.dayOfMonth
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (taskId == null) "Add Task to Calendar" else "Edit Calendar Task") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Selected date: ${state.selectedDate.format(dateFormatter)}",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedButton(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Calendar Date")
            }

            OutlinedTextField(
                value = state.title,
                onValueChange = taskViewModel::updateTitle,
                label = { Text("Task title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = taskViewModel::updateDescription,
                label = { Text("Task details") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = state.priority,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Priority") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    priorities.forEach { priority ->
                        DropdownMenuItem(
                            text = { Text(priority) },
                            onClick = {
                                taskViewModel.updatePriority(priority)
                                expanded = false
                            }
                        )
                    }
                }
            }

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { taskViewModel.saveTask(onSaveComplete) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Task on Calendar")
            }
        }
    }
}
