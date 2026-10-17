package com.example.finalDueDateCalendarApp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarTaskDao {
    @Query("SELECT * FROM calendar_tasks WHERE userId = :userId ORDER BY dueDateMillis ASC, isCompleted ASC")
    fun getTasksForUser(userId: Int): Flow<List<CalendarTaskEntity>>

    @Query("SELECT * FROM calendar_tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Int): CalendarTaskEntity?

    @Insert
    suspend fun insertTask(task: CalendarTaskEntity)

    @Update
    suspend fun updateTask(task: CalendarTaskEntity)

    @Delete
    suspend fun deleteTask(task: CalendarTaskEntity)
}
