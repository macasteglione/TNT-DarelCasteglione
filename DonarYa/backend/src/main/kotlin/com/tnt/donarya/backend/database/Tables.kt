package com.tnt.donarya.backend.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Users : Table("users") {
    val id = varchar("id", 64).uniqueIndex()
    val nombre = varchar("nombre", 255)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val rol = varchar("rol", 20) // DONANTE | MERENDERO
    val nombreComedor = varchar("nombre_comedor", 255).nullable()
    val whatsapp = varchar("whatsapp", 50).nullable()
    val direccion = varchar("direccion", 255).nullable()
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

object Merenderos : Table("merenderos") {
    val id = varchar("id", 64).uniqueIndex()
    val userId = varchar("user_id", 64).references(Users.id)
    val name = varchar("name", 255)
    val address = varchar("address", 255).default("")
    val neighborhood = varchar("neighborhood", 255).default("")
    val coordinator = varchar("coordinator", 255)
    val whatsapp = varchar("whatsapp", 50)
    val kidsCount = integer("kids_count").default(0)
    val activeNeeds = integer("active_needs").default(0)
    val coveredNeeds = integer("covered_needs").default(0)
    val isVerified = bool("is_verified").default(false)
    val latitude = double("latitude")
    val longitude = double("longitude")
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

object Needs : Table("needs") {
    val id = varchar("id", 64).uniqueIndex()
    val merenderoId = varchar("merendero_id", 64).references(Merenderos.id)
    val title = varchar("title", 255)
    val description = varchar("description", 500)
    val type = varchar("type", 30)
    val urgency = varchar("urgency", 30)
    val items = text("items") // JSON array
    val publishedMinutesAgo = integer("published_minutes_ago").default(0)
    val donorsOnWay = integer("donors_on_way").default(0)
    val isCovered = bool("is_covered").default(false)
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
