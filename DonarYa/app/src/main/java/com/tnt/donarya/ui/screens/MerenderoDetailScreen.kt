package com.tnt.donarya.ui.screens

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnt.donarya.data.model.SampleData
import com.tnt.donarya.ui.components.ItemChip
import com.tnt.donarya.ui.components.UrgencyBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerenderoDetailScreen(
    merenderoId: String,
    onBack: () -> Unit,
    onVoyParaAllá: () -> Unit
) {
    val merendero = SampleData.merenderos.find { it.id == merenderoId }
        ?: SampleData.merenderos.first()
    val need = merendero.needs.firstOrNull() ?: return

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
                                Icons.Default.ArrowBack,
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
                        "Necesitamos alimentos secos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color(0xFF111827)
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
                        text = "${merendero.coordinator} · Coordinadora"
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

                    // Map placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Map,
                                contentDescription = null,
                                tint = Color(0xFF40916C),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                "${merendero.distanceKm} km · ~${merendero.walkMinutes} min caminando",
                                color = Color(0xFF40916C),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // WhatsApp button
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366))
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color(0xFF25D366)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Contactar por WhatsApp", fontWeight = FontWeight.Medium)
                            Text(
                                merendero.whatsapp + " · Marta",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // CTA
                    Button(
                        onClick = onVoyParaAllá,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Voy para allá", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
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
