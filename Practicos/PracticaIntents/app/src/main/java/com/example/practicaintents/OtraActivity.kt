package com.example.practicaintents

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.practicaintents.databinding.ActivityOtraBinding

class OtraActivity : AppCompatActivity() {
    // 1. Usa ActivityOtraBinding (el de esta pantalla)
    private lateinit var binding: ActivityOtraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 2. Inicializa el binding correctamente
        binding = ActivityOtraBinding.inflate(layoutInflater)
        setContentView(binding.root) // Usa binding.root, NO R.layout.activity_otra

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 3. Obtén los datos (con ?: "" para evitar que diga "null")
        val nombreRecibido = intent.getStringExtra("EXTRA_NOMBRE") ?: ""
        val apellidoRecibido = intent.getStringExtra("EXTRA_APELLIDO") ?: ""

        // 4. Ahora binding.textView funcionará perfectamente
        binding.textView.text = "Hola, $nombreRecibido $apellidoRecibido"
    }
}