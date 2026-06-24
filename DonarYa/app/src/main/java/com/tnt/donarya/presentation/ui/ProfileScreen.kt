package com.tnt.donarya.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tnt.donarya.data.GlobalNotificationObserver
import com.tnt.donarya.data.remote.dto.DonationHistoryDto
import com.tnt.donarya.data.remote.dto.NeedHistoryDto
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.presentation.viewmodel.ProfileViewModel
import com.tnt.donarya.ui.components.DonarYaBottomBar


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onHome: () -> Unit,
    onDonar: () -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit = {},
    roleEnum: UserRole = UserRole.DONANTE,
    onNotifications: () -> Unit = {}
) {

    val viewModel: ProfileViewModel = viewModel()
    val profileData by viewModel.profileData.collectAsState()
    val user = profileData.user
    val unreadCount by GlobalNotificationObserver.unreadCount.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refresh()
        }
    }

    // Para el historial
    val donationsCount = profileData.donationHistory.size
    val merenderosHelped = profileData.donationHistory
        .map { it.merenderoName }
        .distinct()
        .size
    val needsHistory = profileData.needsHistory

    // PARA LA "FOTO" QUE MUESTRE LAS INICIALES
    val initials = user?.nombre
        ?.split(" ")
        ?.filter { it.isNotBlank() }
        ?.take(2)
        ?.joinToString("") { it.first().uppercase() }
        ?: "?"

    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            DonarYaBottomBar(
                role = roleEnum,
                currentRoute = "profile",
                unreadNotifications = unreadCount,
                onHome = onHome,
                onDonar = onDonar,
                onPerfil = {},
                onNotifications = onNotifications
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
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "Ajustes",
                                        tint = Color.White
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Editar perfil") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onEditProfile()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Cerrar sesión",
                                                color = Color(0xFFE53935)
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ExitToApp,
                                                contentDescription = null,
                                                tint = Color(0xFFE53935)
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            UserRepositoryImpl.logout()
                                            onLogout()
                                        }
                                    )
                                }
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
                        if (user?.rol != UserRole.MERENDERO) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ImpactStat(
                                    "$donationsCount",
                                    "necesidades\ncompletadas",
                                    null
                                )

                                ImpactStat(
                                    "$merenderosHelped",
                                    "merenderos\nayudados",
                                    null
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ImpactStat(
                                    "${needsHistory.size}",
                                    "necesidades\npublicadas",
                                    null
                                )

                                ImpactStat(
                                    "${needsHistory.count { it.isCovered }}",
                                    "necesidades\ncubiertas",
                                    null
                                )
                            }
                        }
                    }
                }
            }

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
                            val earnedBadges = user?.badges?.filter { it.earned }.orEmpty()
                            if (earnedBadges.isEmpty()) {
                                Text(
                                    "Todavía no tienes ninguna insignia",
                                    fontSize = 13.sp,
                                    color = Color(0xFF9CA3AF),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    earnedBadges.forEach { badge ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(52.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFD8F3DC)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(badge.emoji, fontSize = 24.sp)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                badge.name,
                                                fontSize = 11.sp,
                                                color = Color(0xFF374151)
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

                items(profileData.donationHistory) { donation ->
                    DonationHistoryRow(donation)
                }
            }
            if (user?.rol == UserRole.MERENDERO) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Necesidades completadas",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF111827)
                        )
                    }
                }

                items(profileData.needsHistory) { need ->
                    NeedHistoryRow(need)
                }
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
fun DonationHistoryRow(donation: DonationHistoryDto) {
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
                .background(Color(0xFF40916C).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF40916C)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                donation.needTitle,
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
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = DividerDefaults.Thickness,
        color = Color(0xFFF3F4F6)
    )
}

@Composable
fun NeedHistoryRow(need: NeedHistoryDto) {
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
                .background(Color(0xFF40916C).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF40916C)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                need.title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF111827)
            )
            Text(
                "Cubierta hace ${need.daysAgo} días",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = DividerDefaults.Thickness,
        color = Color(0xFFF3F4F6)
    )
}