package com.tnt.donarya.domain.repository

import com.tnt.donarya.domain.model.User

interface UserRepository {
    fun register(user: User): Result<User>
    fun login(email: String, password: String): Result<User>
    fun getCurrentUser(): User?
    fun logout()
}