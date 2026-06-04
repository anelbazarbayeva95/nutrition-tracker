package com.example.nutritiontracker.data

import android.content.Context
import android.content.SharedPreferences
import com.example.nutritiontracker.data.RDIRequirements
import com.example.nutritiontracker.utils.RDICalculator

class SettingsRepository(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_AGE = "age"
        private const val KEY_GENDER = "gender"
        private const val KEY_HEIGHT = "height"
        private const val KEY_WEIGHT = "weight"
        private const val KEY_BODY_FAT = "body_fat"
        private const val KEY_ACTIVITY_LEVEL = "activity_level"
    }

    fun saveSettings(settings: SettingsData) {
        sharedPreferences.edit().apply {
            putString(KEY_NAME, settings.name)
            putString(KEY_AGE, settings.age)
            putString(KEY_GENDER, settings.gender)
            putString(KEY_HEIGHT, settings.height)
            putString(KEY_WEIGHT, settings.weight)
            putString(KEY_BODY_FAT, settings.bodyFat)
            putString(KEY_ACTIVITY_LEVEL, settings.activityLevel)
            apply()
        }
    }

    fun loadSettings(): SettingsData {
        return SettingsData(
            name = sharedPreferences.getString(KEY_NAME, "") ?: "",
            age = sharedPreferences.getString(KEY_AGE, "") ?: "",
            gender = sharedPreferences.getString(KEY_GENDER, "") ?: "",
            height = sharedPreferences.getString(KEY_HEIGHT, "") ?: "",
            weight = sharedPreferences.getString(KEY_WEIGHT, "") ?: "",
            bodyFat = sharedPreferences.getString(KEY_BODY_FAT, "") ?: "",
            activityLevel = sharedPreferences.getString(KEY_ACTIVITY_LEVEL, "") ?: ""
        )
    }

    fun getCurrentRdiRequirements(): RDIRequirements {
        val settings = loadSettings()

        val ageInt = settings.age.toIntOrNull() ?: 25
        val weightDouble = settings.weight.toDoubleOrNull() ?: 70.0
        val heightDouble = settings.height.toDoubleOrNull() ?: 170.0
        val gender = settings.gender
        val activityLevel = settings.activityLevel

        return RDICalculator.calculateRDI(
            age = ageInt,
            gender = gender,
            weight = weightDouble,
            height = heightDouble,
            activityLevel = activityLevel
        )
    }

    fun clearSettings() {
        sharedPreferences.edit().clear().apply()
    }
}