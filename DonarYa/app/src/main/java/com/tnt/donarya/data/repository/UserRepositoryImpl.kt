package com.tnt.donarya.data.repository

import android.content.Context
import com.tnt.donarya.data.local.LocalStorage
import com.tnt.donarya.domain.model.User
import com.tnt.donarya.domain.repository.UserRepository

object UserRepositoryImpl : UserRepository {

    private val users = mutableListOf<User>()

    private lateinit var storage: LocalStorage



    private var currentUser: User? = null

    override fun register(
        user: User
    ): Result<User> {

        users.add(user)

        storage.saveUsers(users)

        currentUser = user

        storage.saveLoggedUser(user.email)

        return Result.success(user)
    }

    override fun login(
        email: String,
        password: String
    ): Result<User> {

        val user = users.find {
            it.email == email &&
                    it.password == password
        }

        return if (user != null) {

            currentUser = user

            storage.saveLoggedUser(user.email)

            Result.success(user)

        } else {

            Result.failure(
                Exception("Usuario o contraseña incorrectos")
            )
        }
    }

    override fun getCurrentUser(): User? {
        return currentUser
    }

    override fun logout() {
        currentUser = null

        storage.clearSession()
    }

    fun init(context: Context) {

        storage = LocalStorage(context)

        val savedUsers = storage.getUsers()

        if (savedUsers.isNotEmpty()) {
            users.clear()
            users.addAll(savedUsers)
        }
    }

    fun restoreSession(): User? {

        val email = storage.getLoggedUser()

        currentUser = users.find {
            it.email == email
        }

        return currentUser
    }
}