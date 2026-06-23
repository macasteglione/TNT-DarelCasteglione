package com.tnt.donarya.backend.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import java.io.FileInputStream

object FirebaseService {
    private var isInitialized = false

    fun init() {
        try {
            // Intentar cargar desde el archivo de service account
            // El usuario debe poner su service-account.json en la raíz del backend o configurar una variable de entorno
            val serviceAccount = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON") 
                ?: "service-account.json"
            
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(FileInputStream(serviceAccount)))
                .build()

            FirebaseApp.initializeApp(options)
            isInitialized = true
            println("Firebase Admin initialized successfully")
        } catch (e: Exception) {
            println("Warning: Firebase Admin could not be initialized: ${e.message}")
            println("Push notifications will not be sent.")
        }
    }

    fun sendToTopic(topic: String, title: String, body: String) {
        if (!isInitialized) return

        val message = Message.builder()
            .setTopic(topic)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .putData("title", title)
            .putData("message", body)
            .build()

        try {
            val response = FirebaseMessaging.getInstance().send(message)
            println("Successfully sent message: $response")
        } catch (e: Exception) {
            println("Error sending message to topic $topic: ${e.message}")
        }
    }
}
