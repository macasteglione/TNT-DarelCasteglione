package com.tnt.donarya.data.repository

import android.content.Context
import com.tnt.donarya.data.local.LocalStorage
import com.tnt.donarya.data.remote.ApiClient
import com.tnt.donarya.data.remote.TokenStorage
import com.tnt.donarya.data.remote.dto.LoginRequestDto
import com.tnt.donarya.data.remote.dto.RegisterRequestDto
import com.tnt.donarya.domain.model.Badge
import com.tnt.donarya.domain.model.DonationRecord
import com.tnt.donarya.domain.model.NeedType
import com.tnt.donarya.domain.model.User
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

object UserRepositoryImpl : UserRepository {

    private val users = mutableListOf<User>()
    private lateinit var storage: LocalStorage
    private var currentUser: User? = null

    override fun register(user: User, latitude: Double, longitude: Double): Result<User> = runBlocking(Dispatchers.IO) {
        val rol = if (user.rol == UserRole.MERENDERO) "MERENDERO" else "DONANTE"
        val response = ApiClient.register(
            RegisterRequestDto(
                nombre = user.nombre,
                email = user.email,
                password = user.password,
                rol = rol,
                nombreComedor = user.nombreComedor,
                whatsapp = user.whatsapp,
                direccion = user.direccion,
                latitude = latitude,
                longitude = longitude
            )
        )
        response.map { dto ->
            val newUser = User(
                id = dto.user.id,
                nombre = dto.user.nombre,
                email = dto.user.email,
                password = user.password,
                rol = if (dto.user.rol == "MERENDERO") UserRole.MERENDERO else UserRole.DONANTE,
                nombreComedor = dto.user.nombreComedor,
                whatsapp = dto.user.whatsapp,
                direccion = dto.user.direccion,
                merenderoId = dto.user.merenderoId,
                donationsCount = dto.user.donationsCount,
                mendecerosHelped = dto.user.mendecerosHelped,
                beneficiados = dto.user.beneficiados
            )
            TokenStorage.saveToken(dto.token)
            users.add(newUser)
            storage.saveUsers(users)
            currentUser = newUser
            storage.saveLoggedUser(newUser.email)
            newUser
        }
    }

    override fun login(email: String, password: String): Result<User> = runBlocking(Dispatchers.IO) {
        val response = ApiClient.login(LoginRequestDto(email, password))
        response.map { dto ->
            val user = User(
                id = dto.user.id,
                nombre = dto.user.nombre,
                email = dto.user.email,
                password = password,
                rol = if (dto.user.rol == "MERENDERO") UserRole.MERENDERO else UserRole.DONANTE,
                nombreComedor = dto.user.nombreComedor,
                whatsapp = dto.user.whatsapp,
                direccion = dto.user.direccion,
                merenderoId = dto.user.merenderoId,
                donationsCount = dto.user.donationsCount,
                mendecerosHelped = dto.user.mendecerosHelped,
                beneficiados = dto.user.beneficiados
            )
            TokenStorage.saveToken(dto.token)
            currentUser = user
            storage.saveLoggedUser(user.email)
            if (users.none { it.id == user.id }) users.add(user)
            user
        }
    }

    override fun getCurrentUser(): User? = currentUser

    override fun logout() {
        currentUser = null
        TokenStorage.clear()
        storage.clearSession()
    }

    fun init(context: Context) {
        storage = LocalStorage(context)
        TokenStorage.init(context)
        val savedUsers = storage.getUsers()
        if (savedUsers.isNotEmpty()) {
            users.clear()
            users.addAll(savedUsers)
        }
    }

    fun restoreSession(): User? {
        val email = storage.getLoggedUser()
        currentUser = users.find { it.email == email }
        return currentUser
    }
}
