package com.example.finalDueDateCalendarApp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.finalDueDateCalendarApp.data.local.CalendarTaskEntity
import com.example.finalDueDateCalendarApp.data.repository.CalendarTaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class CalendarTaskUiState(
    val tasks: List<CalendarTaskEntity> = emptyList(),
    val title: String = "",
    val description: String = "",
    val priority: String = "Medium",
    val selectedDate: LocalDate = LocalDate.now(),
    val visibleMonth: YearMonth = YearMonth.now(),
    val errorMessage: String? = null,
    val currentUserId: Int? = null,
    val currentTask: CalendarTaskEntity? = null
)

class CalendarTaskViewModel(private val repository: CalendarTaskRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarTaskUiState())
    val uiState: StateFlow<CalendarTaskUiState> = _uiState.asStateFlow()

    private var observeTasksJob: Job? = null

    fun startObservingTasks(userId: Int) {
        _uiState.update { it.copy(currentUserId = userId) }
        observeTasksJob?.cancel()
        observeTasksJob = viewModelScope.launch {
            repository.getTasksForUser(userId).collect { tasks ->
                _uiState.update { it.copy(tasks = tasks) }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, visibleMonth = YearMonth.from(date)) }
    }

    fun previousMonth() {
        _uiState.update { it.copy(visibleMonth = it.visibleMonth.minusMonths(1)) }
    }

    fun nextMonth() {
        _uiState.update { it.copy(visibleMonth = it.visibleMonth.plusMonths(1)) }
    }

    fun updateTitle(value: String) = _uiState.update { it.copy(title = value) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value) }
    fun updatePriority(value: String) = _uiState.update { it.copy(priority = value) }
    fun updateSelectedDate(value: LocalDate) = _uiState.update { it.copy(selectedDate = value, visibleMonth = YearMonth.from(value)) }

    fun prepareNewTaskForSelectedDate() {
        _uiState.update {
            it.copy(
                title = "",
                description = "",
                priority = "Medium",
                currentTask = null,
                errorMessage = null
            )
        }
    }

    fun loadTask(taskId: Int?) {
        if (taskId == null) {
            prepareNewTaskForSelectedDate()
            return
        }

        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            _uiState.update {
                it.copy(
                    title = task?.title.orEmpty(),
                    description = task?.description.orEmpty(),
                    priority = task?.priority ?: "Medium",
                    selectedDate = task?.dueDateMillis?.let(::millisToDate) ?: it.selectedDate,
                    visibleMonth = task?.dueDateMillis?.let { millis -> YearMonth.from(millisToDate(millis)) } ?: it.visibleMonth,
                    currentTask = task,
                    errorMessage = null
                )
            }
        }
    }

    fun saveTask(onComplete: () -> Unit) {
        val state = _uiState.value
        val userId = state.currentUserId
        if (userId == null) {
            _uiState.update { it.copy(errorMessage = "No logged-in user found") }
            return
        }
        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Title cannot be empty") }
            return
        }

        viewModelScope.launch {
            val existing = state.currentTask
            val task = CalendarTaskEntity(
                id = existing?.id ?: 0,
                title = state.title.trim(),
                description = state.description.trim(),
                dueDateMillis = dateToMillis(state.selectedDate),
                priority = state.priority,
                isCompleted = existing?.isCompleted ?: false,
                userId = userId
            )

            if (existing == null) repository.addTask(task) else repository.updateTask(task)

            _uiState.update {
                it.copy(
                    title = "",
                    description = "",
                    priority = "Medium",
                    currentTask = null,
                    errorMessage = null
                )
            }
            onComplete()
        }
    }

    fun toggleTaskCompletion(task: CalendarTaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: CalendarTaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    companion object {
        fun dateToMillis(date: LocalDate): Long =
            date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        fun millisToDate(millis: Long): LocalDate =
            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}

class CalendarTaskViewModelFactory(private val repository: CalendarTaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarTaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarTaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
