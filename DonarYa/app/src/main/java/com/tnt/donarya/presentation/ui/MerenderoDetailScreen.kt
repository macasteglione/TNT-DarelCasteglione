package com.tnt.donarya.presentation.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.presentation.state.MerenderoDetailUiState
import com.tnt.donarya.presentation.viewmodel.MerenderoDetailViewModel
import com.tnt.donarya.ui.components.ItemChip
import com.tnt.donarya.ui.components.UrgencyBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerenderoDetailScreen(
    needId: String,
    onBack: () -> Unit
) {
    val viewModel: MerenderoDetailViewModel = viewModel()

    val confirmState by viewModel.confirmState.collectAsState()

    LaunchedEffect(needId) {
        viewModel.loadNeed(needId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    when (uiState) {

        is MerenderoDetailUiState.Loading -> {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Cargando...")
            }
        }

        is MerenderoDetailUiState.NotFound -> {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Merendero no encontrado")
            }
        }

        is MerenderoDetailUiState.Success -> {

            val state = uiState as MerenderoDetailUiState.Success

            val merendero = state.data.merendero

            val need = state.data.needs.firstOrNull() ?: return

            val needId = state.data.needs
                .firstOrNull { !it.isCovered }
                ?.id

            Scaffold(
                containerColor = Color(0xFFF9FAFB)
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    item {
                        // Dark header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1B4332))
                                .padding(16.dp)
                        ) {
                            Column {
                                IconButton(onClick = onBack) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                UrgencyBadge(need.urgency)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Merendero ${merendero.name}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                )
                                if (merendero.isVerified) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = Color(0xFF52B788),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            " Verificado municipalmente",
                                            color = Color(0xFF52B788),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                need.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color(0xFF1B4332)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Meta info
                            InfoRow(
                                icon = Icons.Default.LocationOn,
                                text = "${merendero.address}, ${merendero.neighborhood}"
                            )
                            InfoRow(
                                icon = Icons.Default.Schedule,
                                text = "Publicado hace ${need.publishedMinutesAgo} minutos"
                            )
                            InfoRow(
                                icon = Icons.Default.Person,
                                text = "${merendero.coordinator} · Coordinador"
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            SectionTitle("Descripción")
                            Text(
                                need.description,
                                color = Color(0xFF374151),
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            SectionTitle("Qué necesitan")
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(need.items) { item ->
                                    ItemChip(item)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            SectionTitle("Cómo llegar")
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    val address = "${merendero.address}, ${merendero.neighborhood}"
                                    val hasCoords =
                                        merendero.latitude != 0.0 && merendero.longitude != 0.0
                                    val uri = if (hasCoords) {
                                        "geo:${merendero.latitude},${merendero.longitude}?q=${merendero.latitude},${merendero.longitude}(Merendero ${merendero.name})".toUri()
                                    } else {
                                        "geo:0,0?q=${Uri.encode(address)}".toUri()
                                    }
                                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF4285F4)
                                )
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF4285F4)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Tocar para abrir en Google Maps",
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // WhatsApp button
                            OutlinedButton(

                                onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        "https://wa.me/${
                                            merendero.whatsapp.replace(
                                                "+",
                                                ""
                                            ).replace(" ", "")
                                        }".toUri()
                                    )
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(
                                        0xFF25D366
                                    )
                                )
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = Color(0xFF25D366)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Contactar por WhatsApp", fontWeight = FontWeight.Medium)
                                    Text(
                                        merendero.whatsapp,
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val currentUser = UserRepositoryImpl.getCurrentUser()
                            if (currentUser?.rol == UserRole.DONANTE) {
                                Button(
                                    onClick = {
                                        if (needId != null) {
                                            viewModel.confirmarDonacion(needId)
                                        }
                                    },
                                    enabled = confirmState !is MerenderoDetailViewModel.ConfirmState.Loading
                                            && confirmState !is MerenderoDetailViewModel.ConfirmState.Success,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (confirmState) {
                                            is MerenderoDetailViewModel.ConfirmState.Success -> Color(
                                                0xFF22C55E
                                            )

                                            else -> Color(0xFF1B4332)
                                        },
                                        contentColor = Color.White,
                                        disabledContainerColor = when (confirmState) {
                                            is MerenderoDetailViewModel.ConfirmState.Success -> Color(
                                                0xFF22C55E
                                            )

                                            else -> Color(0xFF1B4332)
                                        },
                                        disabledContentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    when (confirmState) {
                                        is MerenderoDetailViewModel.ConfirmState.Loading ->
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )

                                        is MerenderoDetailViewModel.ConfirmState.Success ->
                                            Text(
                                                "✓ ¡Avisaste que vas!",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )

                                        is MerenderoDetailViewModel.ConfirmState.Error ->
                                            Text(
                                                "Error — intentá de nuevo",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )

                                        else ->
                                            Text(
                                                "Voy para allá",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                    }
                                }

                                // Mostrar error como texto si falló
                                if (confirmState is MerenderoDetailViewModel.ConfirmState.Error) {
                                    Text(
                                        (confirmState as MerenderoDetailViewModel.ConfirmState.Error).message,
                                        color = Color(0xFFE63946),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            } // end if DONANTE
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(16.dp)
        )
        Text(
            " $text",
            fontSize = 13.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF111827))
}
