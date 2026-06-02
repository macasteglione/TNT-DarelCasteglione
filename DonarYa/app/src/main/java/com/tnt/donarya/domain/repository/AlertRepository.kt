package com.tnt.donarya.domain.repository

import com.tnt.donarya.domain.model.AlertItem

interface AlertRepository {
    fun getAll(): List<AlertItem>
}