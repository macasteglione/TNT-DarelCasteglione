package com.tnt.donarya.domain.usecase

import com.tnt.donarya.domain.repository.NeedRepository

class MarkNeedCoveredUseCase(private val repository: NeedRepository) {

    operator fun invoke(needId: String): Result<Unit> {
        if (needId.isBlank())
            return Result.failure(Exception("ID de necesidad inválido"))

        return repository.markAsCovered(needId)
    }
}