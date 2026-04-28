package com.tnt.donarya.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.tnt.donarya.data.model.Merendero
import com.tnt.donarya.data.model.SampleData
import com.tnt.donarya.data.model.UrgencyLevel
import com.tnt.donarya.ui.components.DonarYaBottomBar
import com.tnt.donarya.ui.components.StatCard
import com.tnt.donarya.ui.components.UrgencyBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerenderoListScreen(
    onMerenderoClick: (String) -> Unit,
    onAlertas: () -> Unit,
    onPerfil: () -> Unit
) {
    val merenderos = SampleData.merenderos

    Scaffold(
        bottomBar = {
            DonarYaBottomBar(
                currentRoute = "merendero_list",
                onAlertas = onAlertas,
                onDonar = {},
                onPerfil = onPerfil,
                alertCount = 3
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2D6A4F))
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                "Buenos días, Julián 👋",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                            Text(
                                "Lista de\nMerenderos",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                lineHeight = 32.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF48C06)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "JM",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            item {
                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1B4332))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard("3", "Activos", modifier = Modifier.weight(1f))
                    Divider(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    )
                    StatCard("34", "Chicos", modifier = Modifier.weight(1f))
                }
            }

            item {
                // Location header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        " Puerto Madryn, Argentina",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                }
            }

            items(merenderos) { merendero ->
                MerenderoCard(
                    merendero = merendero,
                    onClick = { onMerenderoClick(merendero.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun MerenderoCard(
    merendero: Merendero,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val urgentNeed = merendero.needs.firstOrNull { it.urgency == UrgencyLevel.URGENTE }
    val topNeed = merendero.needs.firstOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (urgentNeed != null) {
                        UrgencyBadge(urgentNeed.urgency)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    Text(
                        merendero.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF111827)
                    )
                    Text(
                        merendero.address,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    text = "hace ${if ((urgentNeed?.publishedMinutesAgo ?: 0) < 60) "${urgentNeed?.publishedMinutesAgo} min" else "2 días"}",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            topNeed?.let { need ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    need.items.joinToString(", "),
                    fontSize = 13.sp,
                    color = Color(0xFF374151)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val donorsOnWay = merendero.needs.sumOf { it.donorsOnWay }
                if (donorsOnWay > 0) {
                    Icon(
                        Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = Color(0xFF40916C),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        " $donorsOnWay donantes en camino",
                        fontSize = 12.sp,
                        color = Color(0xFF40916C),
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text("Sin respuestas aún", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                }
            }
        }
    }
}
