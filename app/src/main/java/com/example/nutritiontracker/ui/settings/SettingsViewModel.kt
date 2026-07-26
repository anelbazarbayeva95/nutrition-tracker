package com.example.nutritiontracker.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.nutritiontracker.data.SettingsData
import com.example.nutritiontracker.data.SettingsRepository

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)

    private val _settings = MutableLiveData<SettingsData>()
    val settings: LiveData<SettingsData> = _settings

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    init {
        loadSettings()
    }

    fun loadSettings() {
        _settings.value = repository.loadSettings()
    }

    fun saveSettings(settings: SettingsData) {
        repository.saveSettings(settings)
        _settings.value = settings
        _saveSuccess.value = true
    }

    fun clearSaveSuccessFlag() {
        _saveSuccess.value = false
    }

    fun clearAllSettings() {
        repository.clearSettings()
        loadSettings()
    }
}
