package com.tnt.donarya.data.repository

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.tnt.donarya.data.remote.dto.UpdateProfileRequestDto
import com.tnt.donarya.domain.model.User
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

object FirebaseUserRepository : UserRepository {

    private val auth = FirebaseAuth.getInstance()

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    private var currentUser: User? = null

    override fun register(user: User, latitude: Double, longitude: Double): Result<User> {
        return runBlocking(Dispatchers.IO) {
            try {
                val authResult =
                    auth.createUserWithEmailAndPassword(user.email, user.password).await()
                val firebaseUser = authResult.user
                    ?: return@runBlocking Result.failure(Exception("Error al crear usuario"))

                val uid = firebaseUser.uid
                val rol = if (user.rol == UserRole.MERENDERO) "MERENDERO" else "DONANTE"
                val merenderoId = if (user.rol == UserRole.MERENDERO) uid else null

                val userData = hashMapOf(
                    "nombre" to user.nombre,
                    "email" to user.email,
                    "rol" to rol,
                    "nombreComedor" to (user.nombreComedor ?: ""),
                    "whatsapp" to (user.whatsapp ?: ""),
                    "direccion" to (user.direccion ?: ""),
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "merenderoId" to merenderoId,
                    "donationsCount" to 0,
                    "mendecerosHelped" to 0,
                    "beneficiados" to 0
                )

                usersCollection.document(uid).set(userData).await()

                if (merenderoId != null) {
                    val merenderoData = hashMapOf(
                        "name" to (user.nombreComedor ?: user.nombre),
                        "address" to (user.direccion ?: ""),
                        "neighborhood" to "",
                        "coordinator" to user.nombre,
                        "whatsapp" to (user.whatsapp ?: ""),
                        "kidsCount" to 0,
                        "activeNeeds" to 0,
                        "coveredNeeds" to 0,
                        "isVerified" to false,
                        "distanceKm" to 0.0,
                        "walkMinutes" to 0,
                        "latitude" to latitude,
                        "longitude" to longitude
                    )
                    db.collection("merenderos").document(uid).set(merenderoData).await()
                }

                val newUser = User(
                    id = uid,
                    nombre = user.nombre,
                    email = user.email,
                    password = user.password,
                    rol = user.rol,
                    nombreComedor = user.nombreComedor,
                    whatsapp = user.whatsapp,
                    direccion = user.direccion,
                    merenderoId = merenderoId
                )
                currentUser = newUser

                if (newUser.rol == UserRole.DONANTE) {
                    FirebaseMessaging.getInstance().subscribeToTopic("donors")
                }

                Result.success(newUser)
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains(
                        "email address is already in use",
                        ignoreCase = true
                    ) == true ->
                        "Este email ya está registrado"

                    e.message?.contains("password", ignoreCase = true) == true ->
                        "La contraseña debe tener al menos 6 caracteres"

                    e.message?.contains("invalid email", ignoreCase = true) == true ->
                        "Email inválido"

                    else -> e.message ?: "Error al registrar"
                }
                Result.failure(Exception(msg))
            }
        }
    }

    override fun login(email: String, password: String): Result<User> {
        return runBlocking(Dispatchers.IO) {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                    ?: return@runBlocking Result.failure(Exception("Error al iniciar sesión"))

                val uid = firebaseUser.uid
                val doc = usersCollection.document(uid).get().await()

                if (!doc.exists()) {
                    return@runBlocking Result.failure(Exception("Usuario no encontrado"))
                }

                val data = doc.data
                    ?: return@runBlocking Result.failure(Exception("Error al cargar perfil"))

                val user = User(
                    id = uid,
                    nombre = data["nombre"] as? String ?: "",
                    email = data["email"] as? String ?: "",
                    password = password,
                    rol = if ((data["rol"] as? String) == "MERENDERO") UserRole.MERENDERO else UserRole.DONANTE,
                    nombreComedor = data["nombreComedor"] as? String,
                    whatsapp = data["whatsapp"] as? String,
                    direccion = data["direccion"] as? String,
                    merenderoId = data["merenderoId"] as? String,
                    donationsCount = (data["donationsCount"] as? Long)?.toInt() ?: 0,
                    mendecerosHelped = (data["mendecerosHelped"] as? Long)?.toInt() ?: 0,
                    beneficiados = (data["beneficiados"] as? Long)?.toInt() ?: 0
                )

                currentUser = user

                if (user.rol == UserRole.DONANTE) {
                    FirebaseMessaging.getInstance().subscribeToTopic("donors")
                }

                Result.success(user)
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("password is invalid", ignoreCase = true) == true ||
                            e.message?.contains("no user record", ignoreCase = true) == true ->
                        "Email o contraseña incorrectos"

                    e.message?.contains("too many requests", ignoreCase = true) == true ->
                        "Demasiados intentos. Esperá unos minutos"

                    else -> e.message ?: "Error al iniciar sesión"
                }
                Result.failure(Exception(msg))
            }
        }
    }

    override fun getCurrentUser(): User? = currentUser

    override fun logout() {
        if (currentUser?.rol == UserRole.DONANTE) {
            try {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("donors")
            } catch (_: Exception) {
            }
        }
        auth.signOut()
        currentUser = null
    }

    override suspend fun updateProfile(req: UpdateProfileRequestDto): Result<User> {
        val uid = currentUser?.id ?: return Result.failure(Exception("No hay usuario logueado"))
        return try {
            val updates = hashMapOf<String, Any>(
                "nombre" to req.nombre,
                "email" to req.email,
                "nombreComedor" to (req.nombreComedor ?: ""),
                "whatsapp" to (req.whatsapp ?: ""),
                "direccion" to (req.direccion ?: "")
            )
            if (req.latitude != null) updates["latitude"] = req.latitude
            if (req.longitude != null) updates["longitude"] = req.longitude

            usersCollection.document(uid).update(updates as Map<String, Any>).await()

            val updatedUser = currentUser!!.copy(
                nombre = req.nombre,
                email = req.email,
                nombreComedor = req.nombreComedor,
                whatsapp = req.whatsapp,
                direccion = req.direccion
            )
            currentUser = updatedUser
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun restoreSession(): User? {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            currentUser = null
            return null
        }
        val uid = firebaseUser.uid
        return runBlocking(Dispatchers.IO) {
            try {
                val doc = usersCollection.document(uid).get().await()
                if (!doc.exists()) return@runBlocking null
                val data = doc.data ?: return@runBlocking null
                val user = User(
                    id = uid,
                    nombre = data["nombre"] as? String ?: "",
                    email = data["email"] as? String ?: "",
                    password = "",
                    rol = if ((data["rol"] as? String) == "MERENDERO") UserRole.MERENDERO else UserRole.DONANTE,
                    nombreComedor = data["nombreComedor"] as? String,
                    whatsapp = data["whatsapp"] as? String,
                    direccion = data["direccion"] as? String,
                    merenderoId = data["merenderoId"] as? String,
                    donationsCount = (data["donationsCount"] as? Long)?.toInt() ?: 0,
                    mendecerosHelped = (data["mendecerosHelped"] as? Long)?.toInt() ?: 0,
                    beneficiados = (data["beneficiados"] as? Long)?.toInt() ?: 0
                )
                currentUser = user

                if (user.rol == UserRole.DONANTE) {
                    FirebaseMessaging.getInstance().subscribeToTopic("donors")
                }

                user
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun getDonationHistory(): List<com.tnt.donarya.data.remote.dto.DonationHistoryDto> {
        val userId = currentUser?.id ?: return emptyList()
        return try {
            val confirmations = db.collection("donorConfirmations")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val result = mutableListOf<com.tnt.donarya.data.remote.dto.DonationHistoryDto>()
            for (doc in confirmations.documents) {
                val needId = doc.data?.get("needId") as? String ?: continue
                val needDoc = db.collection("needs").document(needId).get().await()
                val needData = needDoc.data ?: continue
                val isCovered = needData["isCovered"] as? Boolean ?: false
                if (!isCovered) continue

                val merenderoId = needData["merenderoId"] as? String ?: ""
                val merenderoDoc = db.collection("merenderos").document(merenderoId).get().await()
                val merenderoName = merenderoDoc.data?.get("name") as? String ?: ""

                val createdAt = needData["createdAt"] as? com.google.firebase.Timestamp
                val daysAgo = if (createdAt != null) {
                    val diff = System.currentTimeMillis() - createdAt.toDate().time
                    (diff / (1000 * 60 * 60 * 24)).toInt()
                } else 0

                result.add(
                    com.tnt.donarya.data.remote.dto.DonationHistoryDto(
                        needTitle = needData["title"] as? String ?: "",
                        needType = needData["type"] as? String ?: "",
                        merenderoName = merenderoName,
                        daysAgo = daysAgo
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getNeedsHistory(): List<com.tnt.donarya.data.remote.dto.NeedHistoryDto> {
        val merenderoId = currentUser?.merenderoId ?: return emptyList()
        return try {
            val needs = db.collection("needs")
                .whereEqualTo("merenderoId", merenderoId)
                .get()
                .await()

            needs.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val createdAt = data["createdAt"] as? com.google.firebase.Timestamp
                val daysAgo = if (createdAt != null) {
                    val diff = System.currentTimeMillis() - createdAt.toDate().time
                    (diff / (1000 * 60 * 60 * 24)).toInt()
                } else 0
                com.tnt.donarya.data.remote.dto.NeedHistoryDto(
                    title = data["title"] as? String ?: "",
                    type = data["type"] as? String ?: "",
                    daysAgo = daysAgo,
                    isCovered = data["isCovered"] as? Boolean ?: false,
                    donorsOnWay = (data["donorsOnWay"] as? Long)?.toInt() ?: 0
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
