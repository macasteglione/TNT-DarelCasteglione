package com.tnt.donarya.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tnt.donarya.domain.model.NotificationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNeedClick: (String) -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1B4332)
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF40916C))
            }
        } else if (state.notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFD1D5DB)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No hay notificaciones",
                        fontSize = 16.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(state.notifications, key = { it.id }) { noti ->
                    NotificationCard(
                        noti = noti,
                        onClick = {
                            if (!noti.isRead) viewModel.markAsRead(noti.id)
                            noti.relatedNeedId?.let { onNeedClick(it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    noti: NotificationItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (noti.isRead) Color.White else Color(0xFFF0FDF4)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (!noti.isRead) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .padding(top = 6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF40916C))
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = noti.message,
                    fontSize = 14.sp,
                    fontWeight = if (noti.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatNotiDate(noti.createdAt),
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
        HorizontalDivider(color = Color(0xFFF3F4F6))
    }
}

private fun formatNotiDate(iso: String): String {
    return try {
        val parts = iso.split("T")
        if (parts.size >= 2) {
            val date = parts[0]
            val time = parts[1].substring(0, minOf(5, parts[1].length))
            "$date $time"
        } else iso
    } catch (_: Exception) { iso }
}
