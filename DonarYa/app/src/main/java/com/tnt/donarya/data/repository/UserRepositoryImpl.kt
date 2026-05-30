package com.tnt.donarya.data.repository

import android.content.Context
import com.tnt.donarya.data.local.LocalStorage
import com.tnt.donarya.domain.model.Badge
import com.tnt.donarya.domain.model.DonationRecord
import com.tnt.donarya.domain.model.NeedType
import com.tnt.donarya.domain.model.User
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.domain.repository.UserRepository

object UserRepositoryImpl : UserRepository {

    private val users = mutableListOf(

        User(
            id = "u1",
            nombre = "Julián Morales",
            email = "julian@gmail.com",
            password = "1234",
            rol = UserRole.DONANTE,
            donationsCount = 17,
            mendecerosHelped = 8,
            beneficiados = 142,
            badges = listOf(
                Badge("Estrella", "⭐", true),
                Badge("Plantador", "🌱", true),
                Badge("Fiel", "💙", true),
                Badge("Trofeo", "🏆", false)
            ),
            recentDonations = listOf(
                DonationRecord(NeedType.ALIMENTOS, "Los Girasoles", 3, true),
                DonationRecord(NeedType.ROPA, "Esperanza", 5, true),
                DonationRecord(NeedType.GAS, "San Cayetano", 12, true)
            )
        ),

        User(
            id = "u2",
            nombre = "Marta González",
            email = "marta@gmail.com",
            password = "1234",
            rol = UserRole.MERENDERO,
            nombreComedor = "Los Girasoles",
            direccion = "Av. San Martín 1240",
            whatsapp = "+54 9 341 555-1234",
            cantidadChicos = 34,
            beneficiados = 34
        )
    )

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