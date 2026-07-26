package com.example.nutritiontracker.data.goals

import com.example.nutritiontracker.data.local.DailyLogDao
import com.example.nutritiontracker.data.local.DailyLogEntity

class GoalsRepository(
    private val dailyLogDao: DailyLogDao
) {

    suspend fun getLastDailyLogs(maxDays: Int): List<DailyLogEntity> {
        // sorted ascending so charts draw left→right correctly
        return dailyLogDao.getLastLogs(maxDays).sortedBy { it.date }
    }

    suspend fun getAllLogs(): List<DailyLogEntity> {
        return dailyLogDao.getAllLogs()
    }

    suspend fun upsertLogs(logs: List<DailyLogEntity>) {
        dailyLogDao.upsertLogs(logs)
    }

    suspend fun upsertLog(log: DailyLogEntity) {
        dailyLogDao.upsertLog(log)
    }
}