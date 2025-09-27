package com.example.fairchildfreecell.settings

object SettingsManager {
    var currentSettings: UserSettings = UserSettings()
        private set // Only SettingsManager can replace the whole object.


    fun updateCardSpacing(newSpacing: CardSpacing) {
        currentSettings = currentSettings.copy(cardSpacing = newSpacing)
    }
}