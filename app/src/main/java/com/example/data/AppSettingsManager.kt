package com.example.data

import android.content.Context
import android.content.SharedPreferences

class AppSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("AbaGoldSettings", Context.MODE_PRIVATE)

    fun getString(key: String, default: String): String = prefs.getString(key, default) ?: default
    fun putString(key: String, value: String) { prefs.edit().putString(key, value).apply() }

    fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)
    fun putBoolean(key: String, value: Boolean) { prefs.edit().putBoolean(key, value).apply() }
    
    fun getInt(key: String, default: Int): Int = prefs.getInt(key, default)
    fun putInt(key: String, value: Int) { prefs.edit().putInt(key, value).apply() }
}
