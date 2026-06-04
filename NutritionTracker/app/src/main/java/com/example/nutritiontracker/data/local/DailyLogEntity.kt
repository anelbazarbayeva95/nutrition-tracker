package com.example.nutritiontracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_log")
data class DailyLogEntity(
    @PrimaryKey
    val date: String,          // e.g. "2025-12-10"
    val caloriesConsumed: Int,
    val caloriesTarget: Int
)