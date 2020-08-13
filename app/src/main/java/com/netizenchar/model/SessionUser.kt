package com.netizenchar.model

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

@SuppressLint("CommitPrefEdits")
class SessionUser(context: Context) {
  private val sharedPreferences: SharedPreferences
  private val sharedPreferencesEditor: SharedPreferences.Editor

  companion object {
    private const val userData = "userData"
  }

  init {
    sharedPreferences = context.getSharedPreferences(userData, Context.MODE_PRIVATE)
    sharedPreferencesEditor = sharedPreferences.edit()
  }

  fun set(id: String, value: String) {
    sharedPreferencesEditor.putString(id, value)
    sharedPreferencesEditor.commit()
  }

  fun get(id: String): String {
    return sharedPreferences.getString(id, "")!!
  }

  fun clear() {
    sharedPreferences.edit().clear().apply()
    sharedPreferencesEditor.clear()
  }
}