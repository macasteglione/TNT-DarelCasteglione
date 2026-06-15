package com.tnt.donarya.domain.usecase

import com.tnt.donarya.domain.model.User
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.domain.repository.UserRepository

class RegisterUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(
        nombre: String,
        email: String,
        password: String,
        rol: UserRole,
        nombreComedor: String? = null,
        whatsapp: String? = null,
        direccion: String? = null,
        latitude: Double = 0.0,
        longitude: Double = 0.0
    ): Result<User> {
        if (nombre.isBlank() || email.isBlank() || password.isBlank())
            return Result.failure(Exception("Completá todos los campos"))

        if (!email.contains("@"))
            return Result.failure(Exception("Email inválido"))

        if (password.length < 6)
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))

        if (rol == UserRole.MERENDERO && nombreComedor.isNullOrBlank())
            return Result.failure(Exception("Ingresá el nombre del comedor"))

        if (rol == UserRole.MERENDERO && whatsapp.isNullOrBlank())
            return Result.failure(Exception("El WhatsApp es obligatorio para merenderos"))

        val id = System.currentTimeMillis().toString()

        val user = User(
            id = id,
            nombre = nombre,
            email = email,
            password = password,
            rol = rol,
            nombreComedor = nombreComedor,
            whatsapp = whatsapp,
            direccion = direccion,
            merenderoId = if (rol == UserRole.MERENDERO) id else null
        )

        return userRepository.register(user, latitude, longitude)
    }
}
