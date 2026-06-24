package com.tnt.donarya.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

data class CountryCode(val code: String, val name: String, val flag: String)

val countryCodes = listOf(
    CountryCode("+49", "Alemania", "🇩🇪"),
    CountryCode("+54", "Argentina", "🇦🇷"),
    CountryCode("+591", "Bolivia", "🇧🇴"),
    CountryCode("+55", "Brasil", "🇧🇷"),
    CountryCode("+56", "Chile", "🇨🇱"),
    CountryCode("+57", "Colombia", "🇨🇴"),
    CountryCode("+506", "Costa Rica", "🇨🇷"),
    CountryCode("+53", "Cuba", "🇨🇺"),
    CountryCode("+593", "Ecuador", "🇪🇨"),
    CountryCode("+1", "EE.UU./Canadá", "🇺🇸"),
    CountryCode("+503", "El Salvador", "🇸🇻"),
    CountryCode("+34", "España", "🇪🇸"),
    CountryCode("+33", "Francia", "🇫🇷"),
    CountryCode("+502", "Guatemala", "🇬🇹"),
    CountryCode("+509", "Haití", "🇭🇹"),
    CountryCode("+504", "Honduras", "🇭🇳"),
    CountryCode("+39", "Italia", "🇮🇹"),
    CountryCode("+52", "México", "🇲🇽"),
    CountryCode("+505", "Nicaragua", "🇳🇮"),
    CountryCode("+507", "Panamá", "🇵🇦"),
    CountryCode("+595", "Paraguay", "🇵🇾"),
    CountryCode("+51", "Perú", "🇵🇪"),
    CountryCode("+44", "Reino Unido", "🇬🇧"),
    CountryCode("+598", "Uruguay", "🇺🇾"),
    CountryCode("+58", "Venezuela", "🇻🇪"),
)

@Composable
fun CountryCodePicker(
    selectedCode: String,
    onCodeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = countryCodes.find { it.code == selectedCode } ?: countryCodes.first()

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${selected.flag} ${selected.code}", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "Seleccionar código",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            countryCodes.forEach { cc ->
                DropdownMenuItem(
                    text = {
                        Text("${cc.flag} ${cc.code}  ${cc.name}", fontSize = 14.sp)
                    },
                    onClick = {
                        onCodeSelected(cc.code)
                        expanded = false
                    }
                )
            }
        }
    }
}
