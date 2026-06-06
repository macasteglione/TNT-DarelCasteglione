package com.tnt.donarya.data.remote

import android.content.Context
import android.content.SharedPreferences

object TokenStorage {
    private const val PREFS_NAME = "donarya_auth"
    private const val KEY_TOKEN = "jwt_token"

    private lateinit var prefs: SharedPreferences

    fun init(ctx: Context) {
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }
}
