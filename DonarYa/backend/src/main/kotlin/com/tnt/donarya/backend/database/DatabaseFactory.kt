package com.tnt.donarya.backend.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

object DatabaseFactory {
    private val ds by lazy {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://ep-blue-salad-acgh6aqv-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require"
            username = "neondb_owner"
            password = "npg_XryfCBS7Jou0"
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        HikariDataSource(config)
    }

    fun init() {
        ds.connection.use { /* test connection */ }
    }

    fun dataSource() = ds
}
