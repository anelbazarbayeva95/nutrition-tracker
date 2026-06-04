package com.example.nutritiontracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DailyLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: DailyLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLogs(logs: List<DailyLogEntity>)

    @Query("SELECT * FROM daily_log WHERE date = :date LIMIT 1")
    suspend fun getLogByDate(date: String): DailyLogEntity?

    @Query("SELECT * FROM daily_log ORDER BY date ASC")
    suspend fun getAllLogs(): List<DailyLogEntity>

    @Query(
        """
        SELECT * FROM daily_log
        ORDER BY date DESC
        LIMIT :maxDays
        """
    )
    suspend fun getLastLogs(maxDays: Int): List<DailyLogEntity>
}