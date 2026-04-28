package com.tnt.donarya.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tnt.donarya.data.model.NeedItem
import com.tnt.donarya.data.model.SampleData
import com.tnt.donarya.ui.components.DonarYaBottomBar
import com.tnt.donarya.ui.components.UrgencyBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerenderoHomeScreen(
    onAlertas: () -> Unit,
    onPublishNeed: () -> Unit,
    onPerfil: () -> Unit
) {
    val merendero = SampleData.merenderos.first()
    val needs = merendero.needs

    Scaffold(
        bottomBar = {
            DonarYaBottomBar(
                currentRoute = "merendero_home",
                onAlertas = onAlertas,
                onDonar = {},
                onPerfil = onPerfil,
                alertCount = 1
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
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1B4332))
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    "Buenos días, Marta 👋",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Merendero\n${merendero.name}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    lineHeight = 30.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF48C06)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("MG", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (merendero.isVerified) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                // Stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2D6A4F))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MerenderoStat("3", "Activas")
                    VerticalDivider()
                    MerenderoStat("12", "Cubiertas")
                    VerticalDivider()
                    MerenderoStat("34", "Chicos")
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Mis necesidades activas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF111827)
                    )
                    Button(
                        onClick = onPublishNeed,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF40916C)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nueva", fontSize = 13.sp)
                    }
                }
            }

            items(needs) { need ->
                NeedManageCard(
                    need = need,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Cubiertas recientemente",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF111827)
                    )
                    TextButton(onClick = {}) {
                        Text("Ver todas", color = Color(0xFF40916C), fontSize = 13.sp)
                    }
                }
            }

            items(
                listOf(
                    "Frazadas y mantas" to "Cubierta",
                    "Harina 0000 (5kg)" to "Cubierta"
                )
            ) { (item, status) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item, fontSize = 14.sp, color = Color(0xFF374151))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF40916C),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            " $status",
                            fontSize = 13.sp,
                            color = Color(0xFF40916C),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Divider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF3F4F6))
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun RowScope.MerenderoStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}

@Composable
fun VerticalDivider() {
    Divider(modifier = Modifier
        .width(1.dp)
        .height(36.dp), color = Color.White.copy(alpha = 0.2f))
}

@Composable
fun NeedManageCard(need: NeedItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    UrgencyBadge(need.urgency)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        need.type.label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF111827)
                    )
                    Text(
                        need.items.joinToString(", ") + " · ${need.publishedMinutesAgo} min",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    "hace ${if (need.publishedMinutesAgo < 60) "${need.publishedMinutesAgo} min" else "${need.publishedMinutesAgo / 60} h"}",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            if (need.donorsOnWay > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = Color(0xFF40916C),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        " ${need.donorsOnWay} donantes en camino",
                        fontSize = 12.sp,
                        color = Color(0xFF40916C),
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Sin respuestas aún", fontSize = 12.sp, color = Color(0xFF9CA3AF))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {},
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF40916C)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Editar", fontSize = 13.sp)
                }
            }
        }
    }
}
