package com.example.practicaintents

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.practicaintents.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización de ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when {
            intent?.action == Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent) // Maneja el texto recibido
                }
            }
        }

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

        // --- EJERCICIO 3: (Mapas y Teléfono) ---

        // A. Mapas
        binding.btnOpenMap.setOnClickListener {
            val location = binding.etLocation.text.toString()
            if (location.isNotEmpty()) {
                val gmmIntentUri = Uri.parse("geo:0,0?q=$location")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                startActivity(mapIntent)
            }
        }

        // B. Telefonos
        binding.btnDial.setOnClickListener {
            val phone = binding.etPhone.text.toString()
            if (phone.isNotEmpty()) {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                startActivity(dialIntent)
            }
        }




        // --- EJERCICIO 4: Cámara ---
        val tomarFotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                binding.miImageView.setImageBitmap(it)
            }
        }

        binding.btnCamara.setOnClickListener {
            tomarFotoLauncher.launch(null)// pasar a la cámara
        }




        // --- EJERCICIO 5: Pasar a otra pantalla ---

        binding.btnPasar.setOnClickListener {
            val nombre = binding.etNombre.text.toString()
            val apellido = binding.etApellido.text.toString()

            if (nombre.isNotEmpty() && apellido.isNotEmpty()) {
                val intent = Intent(this, OtraActivity::class.java)

                // Pasamos los datos a la siguiente pantalla
                intent.putExtra("EXTRA_NOMBRE", nombre)
                intent.putExtra("EXTRA_APELLIDO", apellido)

                startActivity(intent)
            } else {
                // Opcional: mostrar un aviso si los campos están vacíos
                Toast.makeText(this, "Por favor, completa ambos campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- EJERCICIO 6: Intent Filters ---
    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { textoRecibido ->
            binding.textFilter.text = textoRecibido
        }
    }
}