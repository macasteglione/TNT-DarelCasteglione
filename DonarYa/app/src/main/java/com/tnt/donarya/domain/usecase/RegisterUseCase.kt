package com.tnt.donarya.domain.usecase

import com.tnt.donarya.domain.model.User
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.domain.repository.UserRepository

class RegisterUseCase(private val repository: UserRepository) {

    operator fun invoke(
        nombre: String,
        email: String,
        password: String,
        rol: UserRole,
        nombreComedor: String? = null,
        whatsapp: String? = null
    ): Result<User> {

        // Validaciones de negocio
        if (nombre.isBlank() || email.isBlank() || password.isBlank())
            return Result.failure(Exception("Completá todos los campos"))

        if (!email.contains("@"))
            return Result.failure(Exception("Email inválido"))

        if (password.length < 6)
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))

        if (rol == UserRole.MERENDERO && nombreComedor.isNullOrBlank())
            return Result.failure(Exception("Ingresá el nombre del comedor"))

        val user = User(
            id = System.currentTimeMillis().toString(),
            nombre = nombre,
            email = email,
            password = password,
            rol = rol,
            nombreComedor = nombreComedor,
            whatsapp = whatsapp
        )

        return repository.register(user)
    }
}