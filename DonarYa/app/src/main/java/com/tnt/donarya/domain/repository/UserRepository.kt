package com.tnt.donarya.domain.repository

import com.tnt.donarya.data.remote.dto.UpdateProfileRequestDto
import com.tnt.donarya.domain.model.User

interface UserRepository {
    fun register(user: User, latitude: Double = 0.0, longitude: Double = 0.0): Result<User>
    fun login(email: String, password: String): Result<User>
    fun getCurrentUser(): User?
    fun logout()

    suspend fun updateProfile(req: UpdateProfileRequestDto): Result<User>
}