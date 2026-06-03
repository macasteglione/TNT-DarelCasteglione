package com.tnt.donarya.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnt.donarya.domain.model.AlertItem
import com.tnt.donarya.domain.model.AlertType
import com.tnt.donarya.ui.components.DonarYaBottomBar
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.tnt.donarya.presentation.state.AlertsUiState
import com.tnt.donarya.presentation.viewmodel.AlertsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    onHome: () -> Unit,
    onDonar: () -> Unit,
    onPerfil: () -> Unit,
    role: com.tnt.donarya.domain.model.UserRole = com.tnt.donarya.domain.model.UserRole.DONANTE
) {
    val viewModel: AlertsViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        "Todas",
        "Urgentes",
        "Respuestas",
        "Sistema"
    )

    when (uiState) {

        is AlertsUiState.Loading -> {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Cargando...")
            }
        }

        is AlertsUiState.Success -> {

            val state = uiState as AlertsUiState.Success

            val alerts = state.alerts

            val unreadCount = alerts.count { !it.isRead }

            Scaffold(
                topBar = {
                    Column(modifier = Modifier.background(Color.White)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Alertas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Color(0xFF111827)
                                )
                                if (unreadCount > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE63946)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "$unreadCount",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            TextButton(onClick = {}) {
                                Text("Marcar leídas", color = Color(0xFF40916C), fontSize = 13.sp)
                            }
                        }
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.White,
                            contentColor = Color(0xFF40916C),
                            edgePadding = 16.dp,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = Color(0xFF40916C)
                                )
                            }
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { Text(tab, fontSize = 14.sp) },
                                    selectedContentColor = Color(0xFF40916C),
                                    unselectedContentColor = Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                },
                bottomBar = {
                    DonarYaBottomBar(
                        role = role,
                        currentRoute = "alerts",
                        onAlertas = {},
                        onHome = onHome,
                        onDonar = onDonar,
                        onPerfil = onPerfil,
                        alertCount = unreadCount
                    )
                },
                containerColor = Color(0xFFF9FAFB)
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    item {
                        Text(
                            "HOY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    items(alerts.take(3)) { alert ->
                        AlertCard(alert)
                    }
                    item {
                        Text(
                            "AYER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    items(alerts.drop(3)) { alert ->
                        AlertCard(alert)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertCard(alert: AlertItem) {
    val (icon, iconBg) = when (alert.type) {
        AlertType.URGENTE -> Icons.Default.Warning to Color(0xFFFFE5E7)
        AlertType.RESPUESTA -> Icons.Default.DirectionsWalk to Color(0xFFD8F3DC)
        AlertType.CUBIERTA -> Icons.Default.CheckCircle to Color(0xFFD8F3DC)
        AlertType.AGRADECIMIENTO -> Icons.Default.Favorite to Color(0xFFFFE5E7)
        AlertType.RECORDATORIO -> Icons.Default.Notifications to Color(0xFFFFF3B0)
        AlertType.SISTEMA -> Icons.Default.Info to Color(0xFFE5E7EB)
    }

    val iconTint = when (alert.type) {
        AlertType.URGENTE -> Color(0xFFE63946)
        AlertType.RESPUESTA, AlertType.CUBIERTA -> Color(0xFF40916C)
        AlertType.AGRADECIMIENTO -> Color(0xFFE63946)
        AlertType.RECORDATORIO -> Color(0xFFF4A261)
        AlertType.SISTEMA -> Color(0xFF6B7280)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!alert.isRead) Color(0xFFF0FDF4) else Color.White)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Left indicator
        if (!alert.isRead && alert.isUrgent) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, end = 8.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE63946))
            )
        } else {
            Spacer(modifier = Modifier.width(16.dp))
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                alert.title,
                fontWeight = if (!alert.isRead) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFF111827)
            )
            Text(
                alert.subtitle,
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 2.dp)
            )

            // Action button for urgent
            if (alert.isUrgent && !alert.isRead) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Ver ahora →", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Text(
            "hace ${if (alert.minutesAgo < 60) "${alert.minutesAgo} min" else if (alert.minutesAgo < 1440) "${alert.minutesAgo / 60} h" else "${alert.minutesAgo / 1440} días"}",
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF)
        )
    }

    Divider(color = Color(0xFFF3F4F6))
}
