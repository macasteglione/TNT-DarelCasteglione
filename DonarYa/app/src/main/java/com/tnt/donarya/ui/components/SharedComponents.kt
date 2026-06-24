package com.tnt.donarya.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnt.donarya.domain.model.UrgencyLevel
import com.tnt.donarya.domain.model.UserRole

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF40916C))
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                "Cargando...",
                color = Color(0xFF6B7280),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun UrgencyBadge(urgency: UrgencyLevel, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (urgency) {
        UrgencyLevel.URGENTE -> Color(0xFFE63946) to Color.White
        UrgencyLevel.ESTA_SEMANA -> Color(0xFFF4A261) to Color.White
        UrgencyLevel.SIN_APURO -> Color(0xFF52B788) to Color.White
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "● ${urgency.label.uppercase()}",
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ItemChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF3F4F6))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF374151),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DonarYaBottomBar(
    role: UserRole,
    currentRoute: String,
    unreadNotifications: Int = 0,
    onHome: () -> Unit,
    onDonar: () -> Unit,
    onPerfil: () -> Unit,
    onNotifications: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        if (role == UserRole.MERENDERO) {
            NavigationBarItem(
                selected = currentRoute == "merendero_home",
                onClick = onHome,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Inicio"
                    )
                },
                label = { Text("Inicio") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF40916C),
                    selectedTextColor = Color(0xFF40916C),
                    unselectedIconColor = Color(0xFF111827),
                    unselectedTextColor = Color(0xFF111827),
                    indicatorColor = Color(0xFFD8F3DC)
                )
            )
        } else {
            NavigationBarItem(
                selected = currentRoute == "merendero_list",
                onClick = onDonar,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Inicio"
                    )
                },
                label = { Text("Inicio") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF40916C),
                    selectedTextColor = Color(0xFF40916C),
                    unselectedIconColor = Color(0xFF111827),
                    unselectedTextColor = Color(0xFF111827),
                    indicatorColor = Color(0xFFD8F3DC)
                )
            )
        }
        NavigationBarItem(
            selected = currentRoute == "notifications",
            onClick = onNotifications,
            icon = {
                Box {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificaciones"
                    )
                    if (unreadNotifications > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-2).dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE63946)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadNotifications > 9) "9+" else "$unreadNotifications",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            },
            label = { Text("Notificaciones") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF40916C),
                selectedTextColor = Color(0xFF40916C),
                unselectedIconColor = Color(0xFF111827),
                unselectedTextColor = Color(0xFF111827),
                indicatorColor = Color(0xFFD8F3DC)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = onPerfil,
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil"
                )
            },
            label = { Text("Perfil") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF40916C),
                selectedTextColor = Color(0xFF40916C),
                unselectedIconColor = Color(0xFF111827),
                unselectedTextColor = Color(0xFF111827),
                indicatorColor = Color(0xFFD8F3DC)
            )
        )
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF40916C))
        Text(label, fontSize = 12.sp, color = Color(0xFF6B7280))
    }
}
