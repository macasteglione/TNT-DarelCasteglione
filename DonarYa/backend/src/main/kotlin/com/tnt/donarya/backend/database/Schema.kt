package com.tnt.donarya.backend.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object Tables {
    val all = listOf(Users, Merenderos, Needs, DonorConfirmations)
}

fun initDatabase() {
    Database.connect(DatabaseFactory.dataSource())
    transaction {
        SchemaUtils.createMissingTablesAndColumns(*Tables.all.toTypedArray())
    }
}
