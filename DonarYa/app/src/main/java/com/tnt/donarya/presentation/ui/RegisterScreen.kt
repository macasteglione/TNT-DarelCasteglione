package com.tnt.donarya.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.presentation.state.RegisterUiState
import com.tnt.donarya.presentation.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    rolInicial: UserRole? = null,
    isEditMode: Boolean = false,
    initialNombre: String = "",
    initialEmail: String = "",
    initialNombreComedor: String = "",
    initialWhatsapp: String = "",
    initialDireccion: String = "",
    onRegisterSuccess: (UserRole) -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var nombre by remember { mutableStateOf(if (isEditMode) initialNombre else "") }
    var email by remember { mutableStateOf(if (isEditMode) initialEmail else "") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(rolInicial) }
    // Campos extra merendero
    var nombreComedor by remember { mutableStateOf(if (isEditMode) initialNombreComedor else "") }
    var whatsapp by remember { mutableStateOf(if (isEditMode) initialWhatsapp else "") }
    var direccion by remember { mutableStateOf(if (isEditMode) initialDireccion else "") }
    val suggestions by viewModel.addressSuggestions.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Success) {
            val user = (uiState as RegisterUiState.Success).user
            onRegisterSuccess(user.rol)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B4332), Color(0xFF2D6A4F), Color(0xFF40916C))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back + título
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Text(
                    if (isEditMode) "Editar perfil" else "Crear cuenta",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                if (isEditMode) "Actualizá tus datos" else "Completá tus datos para empezar",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Selector de rol — se oculta en modo edición
            if (!isEditMode) {
                Text(
                    "¿CÓMO QUERÉS PARTICIPAR?",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RoleCard(
                        title = "Soy\nMerendero",
                        subtitle = "Publico necesidades\ny recibo donaciones",
                        emoji = "🏠",
                        selected = selectedRole == UserRole.MERENDERO,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedRole = UserRole.MERENDERO }
                    )
                    RoleCard(
                        title = "Soy\nDonante",
                        subtitle = "Encuentro merenderos\ny ofrezco ayuda",
                        emoji = "🤝",
                        selected = selectedRole == UserRole.DONANTE,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedRole = UserRole.DONANTE }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.12f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // Nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre completo", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contraseña — solo en registro nuevo
                    if (!isEditMode) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña", color = Color.White.copy(alpha = 0.7f)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Text(
                                        if (passwordVisible) "Ocultar" else "Ver",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            colors = fieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Campos extra para merendero — aparecen animados
                    AnimatedVisibility(visible = selectedRole == UserRole.MERENDERO) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))

                            Divider(color = Color.White.copy(alpha = 0.2f))

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Datos del comedor",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            OutlinedTextField(
                                value = nombreComedor,
                                onValueChange = { nombreComedor = it },
                                label = { Text("Nombre del comedor *", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = fieldColors(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = whatsapp,
                                onValueChange = { whatsapp = it },
                                label = { Text("WhatsApp *", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = fieldColors(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Column {
                                OutlinedTextField(
                                    value = direccion,
                                    onValueChange = {
                                        direccion = it
                                        viewModel.searchAddress(it)
                                    },
                                    label = { Text("Dirección", color = Color.White.copy(alpha = 0.7f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = fieldColors(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                suggestions.forEach { suggestion ->
                                    Text(
                                        text = suggestion.displayName,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        maxLines = 2,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                direccion = suggestion.displayName
                                                viewModel.selectAddress(suggestion)
                                            }
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Error
                    if (uiState is RegisterUiState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            (uiState as RegisterUiState.Error).message,
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón
            Button(
                onClick = {
                    if (isEditMode) {
                        viewModel.actualizar(
                            nombre = nombre,
                            email = email,
                            contrasenia = password,
                            rol = selectedRole ?: UserRole.DONANTE,
                            nombreComedor = nombreComedor.ifBlank { null },
                            whatsapp = whatsapp.ifBlank { null },
                            direccion = direccion.ifBlank { null }
                        )
                    } else {
                        viewModel.actualizar(
                            nombre = nombre,
                            email = email,
                            contrasenia = password,
                            rol = selectedRole ?: UserRole.DONANTE,
                            nombreComedor = nombreComedor.ifBlank { null },
                            whatsapp = whatsapp.ifBlank { null },
                            direccion = direccion.ifBlank { null }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48C06)),
                shape = RoundedCornerShape(14.dp),
                enabled = uiState !is RegisterUiState.Loading
            ) {
                if (uiState is RegisterUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        if (isEditMode) "Guardar cambios" else "Crear cuenta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

        }
    }
}

// Helper para no repetir los colores de los campos
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFFF48C06),
    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
    cursorColor = Color.White
)