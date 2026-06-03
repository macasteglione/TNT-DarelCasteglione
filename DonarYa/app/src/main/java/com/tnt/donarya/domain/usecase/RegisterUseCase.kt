package com.tnt.donarya.domain.usecase

import com.tnt.donarya.domain.model.Merendero
import com.tnt.donarya.domain.model.User
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.domain.repository.MerenderoRepository
import com.tnt.donarya.domain.repository.UserRepository

class RegisterUseCase(
    private val userRepository: UserRepository,
    private val merenderoRepository: MerenderoRepository
) {
    operator fun invoke(
        nombre: String,
        email: String,
        password: String,
        rol: UserRole,
        nombreComedor: String? = null,
        whatsapp: String? = null
    ): Result<User> {
        if (nombre.isBlank() || email.isBlank() || password.isBlank())
            return Result.failure(Exception("Completá todos los campos"))

        if (!email.contains("@"))
            return Result.failure(Exception("Email inválido"))

        if (password.length < 6)
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))

        if (rol == UserRole.MERENDERO && nombreComedor.isNullOrBlank())
            return Result.failure(Exception("Ingresá el nombre del comedor"))

        val id = System.currentTimeMillis().toString()

        val user = User(
            id = id,
            nombre = nombre,
            email = email,
            password = password,
            rol = rol,
            nombreComedor = nombreComedor,
            whatsapp = whatsapp,
            merenderoId = if (rol == UserRole.MERENDERO) id else null
        )

        val result = userRepository.register(user)

        if (result.isSuccess && rol == UserRole.MERENDERO) {
            val merendero = Merendero(
                id = id,
                name = nombreComedor ?: nombre,
                address = "",
                neighborhood = "",
                coordinator = nombre,
                whatsapp = whatsapp ?: "",
                kidsCount = 0,
                activeNeeds = 0,
                coveredNeeds = 0,
                isVerified = false
            )
            merenderoRepository.add(merendero)
        }

        return result
    }
}
