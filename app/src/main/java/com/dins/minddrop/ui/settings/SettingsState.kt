package com.dins.minddrop.ui.settings

import com.dins.minddrop.domain.model.UserPreferences

sealed class SettingsState {
    data object Loading : SettingsState()
    data class Success(val preferences: UserPreferences) : SettingsState()
    data class Error(val message: String) : SettingsState()
}
