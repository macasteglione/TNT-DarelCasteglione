package com.example.practicaintents

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// Asegúrate de que este import coincida con el nombre de tu paquete
import com.example.practicaintents.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización de ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- EJERCICIO 1: Compartir Texto ---
        binding.btnShare.setOnClickListener {
            val textToShare = binding.etShare.text.toString()

            if (textToShare.isEmpty()) {
                Toast.makeText(this, "El texto no puede estar vacío", Toast.LENGTH_SHORT).show()
            } else {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, textToShare)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)

                if (sendIntent.resolveActivity(packageManager) != null) {
                    startActivity(shareIntent)
                } else {
                    // En Android 11+ esto fallará si no agregaste <queries> en el Manifest
                    startActivity(shareIntent)
                }
            }
        }

        // --- EJERCICIO 2: Abrir Navegador ---
        binding.btnOpenBrowser.setOnClickListener {
            var url = binding.etUrl.text.toString()

            if (url.isNotEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }

                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
        }

        // --- EJERCICIO 3: Esquemas URI (Mapas y Teléfono) ---

        // A. Mapas
        binding.btnOpenMap.setOnClickListener {
            val location = binding.etLocation.text.toString()
            if (location.isNotEmpty()) {
                val gmmIntentUri = Uri.parse("geo:0,0?q=$location")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                startActivity(mapIntent)
            }
        }

        // B. Marcaje Telefónico
        binding.btnDial.setOnClickListener {
            val phone = binding.etPhone.text.toString()
            if (phone.isNotEmpty()) {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                startActivity(dialIntent)
            }
        }
    }
}