package com.example.fairchildfreecell.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SavedGameManager {
    private const val PREFS_NAME = "FairchildFreecellPrefs"
    private const val SAVED_GAMES_KEY = "savedGames"
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getSavedGames(): MutableSet<String> {
        return sharedPreferences.getStringSet(SAVED_GAMES_KEY, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    }

    fun isGameSaved(gameNumber: Int): Boolean {
        return getSavedGames().contains(gameNumber.toString())
    }

    fun toggleSaveGame(gameNumber: Int) {
        val savedGames = getSavedGames()
        val gameNumberStr = gameNumber.toString()
        if (savedGames.contains(gameNumberStr)) {
            savedGames.remove(gameNumberStr)
        } else {
            savedGames.add(gameNumberStr)
        }
        sharedPreferences.edit { putStringSet(SAVED_GAMES_KEY, savedGames) }
    }
}