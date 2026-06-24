package com.tnt.donarya.data.repository

import com.tnt.donarya.data.remote.dto.UpdateProfileRequestDto
import com.tnt.donarya.domain.model.User
import com.tnt.donarya.domain.repository.UserRepository

object UserRepositoryImpl : UserRepository {

    override fun register(user: User, latitude: Double, longitude: Double): Result<User> {
        return FirebaseUserRepository.register(user, latitude, longitude)
    }

    override fun login(email: String, password: String): Result<User> {
        return FirebaseUserRepository.login(email, password)
    }

    override fun getCurrentUser(): User? {
        return FirebaseUserRepository.getCurrentUser()
    }

    override fun logout() {
        FirebaseUserRepository.logout()
    }

    override suspend fun updateProfile(req: UpdateProfileRequestDto): Result<User> {
        return FirebaseUserRepository.updateProfile(req)
    }

    fun restoreSession(): User? {
        return FirebaseUserRepository.restoreSession()
    }
}
