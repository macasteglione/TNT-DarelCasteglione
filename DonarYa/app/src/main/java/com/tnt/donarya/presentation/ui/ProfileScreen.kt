package com.tnt.donarya.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnt.donarya.data.repository.MerenderoRepositoryImpl
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.DonationRecord
import com.tnt.donarya.domain.model.NeedType
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.ui.components.DonarYaBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    role: String,
    onAlertas: () -> Unit,
    onHome: () -> Unit,
    onDonar: () -> Unit,
    onLogout: () -> Unit,
    roleEnum: UserRole = UserRole.DONANTE
) {

    val user = UserRepositoryImpl.getCurrentUser()
    val merendero = user?.merenderoId?.let {
        MerenderoRepositoryImpl.getById(it)
    }
    remember { mutableStateOf(false) }

    // PARA LA "FOTO" QUE MUESTRE LAS INICIALES
    val initials = user?.nombre
        ?.split(" ")
        ?.filter { it.isNotBlank() }
        ?.take(2)
        ?.joinToString("") { it.first().uppercase() }
        ?: "?"

    Scaffold(
        bottomBar = {
            DonarYaBottomBar(
                role = roleEnum,
                currentRoute = "profile",
                onAlertas = onAlertas,
                onHome = onHome,
                onDonar = onDonar,
                onPerfil = {}
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                // Profile header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF1B4332), Color(0xFF2D6A4F))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                UserRepositoryImpl.logout()
                                onLogout()
                            }
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Ajustes",
                                    tint = Color.White
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF48C06)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                initials,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            user?.nombre ?: "Invitado",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            if (user?.rol == UserRole.MERENDERO) "Merendero" else "Donante",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            item {
                // Impact card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .offset(y = (-16).dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Mi impacto",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF111827)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            if (user?.rol == UserRole.MERENDERO) {

                                ImpactStat(
                                    "${merendero?.kidsCount ?: user?.cantidadChicos ?: 0}",
                                    "chicos\natendidos",
                                    null
                                )

                                ImpactStat(
                                    "${merendero?.activeNeeds ?: 0}",
                                    "necesidades\nactivas",
                                    "+1 hoy"
                                )

                                ImpactStat(
                                    "${merendero?.coveredNeeds ?: 0}",
                                    "donaciones\nrecibidas",
                                    null
                                )

                            } else {

                                ImpactStat(
                                    "${user?.donationsCount ?: 0}",
                                    "donaciones",
                                    "+5 este mes"
                                )

                                ImpactStat(
                                    "${user?.mendecerosHelped ?: 0}",
                                    "merenderos\nayudados",
                                    null
                                )

                                ImpactStat(
                                    "${user?.beneficiados ?: 0}",
                                    "beneficiados",
                                    null)
                            }
                        }
                    }
                }
            }

            if (user?.rol != UserRole.MERENDERO) {
                item {
                    // Badges
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Insignias",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color(0xFF111827)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                user?.badges?.forEach { badge ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (badge.earned) Color(0xFFD8F3DC) else Color(
                                                        0xFFF3F4F6
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(badge.emoji, fontSize = 24.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            badge.name,
                                            fontSize = 11.sp,
                                            color = if (badge.earned) Color(0xFF374151) else Color(
                                                0xFF9CA3AF
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (user?.rol != UserRole.MERENDERO) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Donaciones recientes",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF111827)
                        )
                        TextButton(onClick = {}) {
                            Text("Ver historial", color = Color(0xFF40916C), fontSize = 13.sp)
                        }
                    }
                }

                items(user?.recentDonations ?: emptyList()) {
                    donation -> DonationRow(donation) }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

        }
    }
}

@Composable
fun ImpactStat(value: String, label: String, extra: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color(0xFF40916C))
        Text(
            label,
            fontSize = 12.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
        extra?.let {
            Text(it, fontSize = 11.sp, color = Color(0xFF52B788), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DonationRow(donation: DonationRecord) {
    val (emoji, color) = when (donation.type) {
        NeedType.ALIMENTOS -> "🍞" to Color(0xFFF4A261)
        NeedType.ROPA -> "👕" to Color(0xFF60A5FA)
        NeedType.GAS -> "🔥" to Color(0xFFE63946)
        NeedType.ABRIGO -> "🧥" to Color(0xFF8B5CF6)
        NeedType.OTROS -> "📦" to Color(0xFF6B7280)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                donation.type.label,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF111827)
            )
            Text(
                "${donation.merenderoName} · hace ${donation.daysAgo} días",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
        if (donation.delivered) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF40916C),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    " Entregado",
                    fontSize = 12.sp,
                    color = Color(0xFF40916C),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
    Divider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF3F4F6))
}
