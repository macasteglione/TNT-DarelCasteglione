package com.tnt.donarya.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tnt.donarya.domain.model.User

class LocalStorage(context: Context) {

    private val prefs =
        context.getSharedPreferences("donarya_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    // -------------------------
    // USERS
    // -------------------------

    fun saveUsers(users: List<User>) {

        val json = gson.toJson(users)

        prefs.edit()
            .putString("users", json)
            .apply()
    }

    fun getUsers(): MutableList<User> {

        val json = prefs.getString("users", null)

        return if (json != null) {

            val type = object : TypeToken<MutableList<User>>() {}.type

            gson.fromJson(json, type)

        } else {
            mutableListOf()
        }
    }

    // -------------------------
    // SESSION
    // -------------------------

    fun saveLoggedUser(email: String) {

        prefs.edit()
            .putString("logged_user", email)
            .apply()
    }

    fun getLoggedUser(): String? {

        return prefs.getString("logged_user", null)
    }

    fun clearSession() {

        prefs.edit()
            .remove("logged_user")
            .apply()
    }
}