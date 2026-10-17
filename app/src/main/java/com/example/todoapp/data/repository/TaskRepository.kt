package com.example.finalDueDateCalendarApp.data.repository

import com.example.finalDueDateCalendarApp.data.local.CalendarTaskDao
import com.example.finalDueDateCalendarApp.data.local.CalendarTaskEntity
import kotlinx.coroutines.flow.Flow

class CalendarTaskRepository(private val taskDao: CalendarTaskDao) {
    fun getTasksForUser(userId: Int): Flow<List<CalendarTaskEntity>> = taskDao.getTasksForUser(userId)

    suspend fun getTaskById(taskId: Int): CalendarTaskEntity? = taskDao.getTaskById(taskId)

    suspend fun addTask(task: CalendarTaskEntity) = taskDao.insertTask(task)

    suspend fun updateTask(task: CalendarTaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: CalendarTaskEntity) = taskDao.deleteTask(task)
}
