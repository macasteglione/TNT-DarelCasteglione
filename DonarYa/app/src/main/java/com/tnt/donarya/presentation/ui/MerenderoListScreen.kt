package com.tnt.donarya.presentation.ui

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tnt.donarya.domain.model.Merendero
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.domain.model.UrgencyLevel
import com.tnt.donarya.presentation.viewmodel.MerenderoListViewModel
import com.tnt.donarya.ui.components.DonarYaBottomBar
import com.tnt.donarya.ui.components.StatCard
import com.tnt.donarya.ui.components.UrgencyBadge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.MerenderoWithNeeds
import com.tnt.donarya.domain.model.NeedItem
import com.tnt.donarya.presentation.state.MerenderoListUiState
import kotlinx.coroutines.flow.collectLatest

import com.tnt.donarya.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerenderoListScreen(
    onNeedClick: (String) -> Unit,
    onPerfil: () -> Unit,
    role: UserRole = UserRole.DONANTE,
    onNotifications: () -> Unit = {}
) {

    val viewModel: MerenderoListViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing =
        (uiState as? MerenderoListUiState.Success)
            ?.isRefreshing ?: false

    val pullState = rememberPullToRefreshState()
    val snackbarHostState = remember { SnackbarHostState() }

    val user = UserRepositoryImpl.getCurrentUser()

    // PARA LA "FOTO" QUE MUESTRE LAS INICIALES
    val initials = user?.nombre
        ?.split(" ")
        ?.filter { it.isNotBlank() }
        ?.take(2)
        ?.joinToString("") { it.first().uppercase() }
        ?: "?"

    LaunchedEffect(Unit) {
        viewModel.notificacion.collectLatest { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    PullToRefreshBox(
        state = pullState,
        isRefreshing = isRefreshing,
        onRefresh = {
            viewModel.refresh()
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                DonarYaBottomBar(
                    role = role,
                    currentRoute = "merendero_list",
                    onHome = {},
                    onDonar = {},
                    onPerfil = onPerfil,
                    onNotifications = onNotifications
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
                                    "Buenos días " + user?.nombre,
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
                                    initials,
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
                        horizontalArrangement = Arrangement.Center
                    ) {
                        StatCard("3", "Activos")
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

                when (uiState) {

                    is MerenderoListUiState.Loading -> {

                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxHeight(0.7f)
                                    .fillMaxWidth()
                            ) {
                                LoadingView()
                            }
                        }
                    }

                    is MerenderoListUiState.Error -> {

                        val message =
                            (uiState as MerenderoListUiState.Error).message

                        item {
                            Text(
                                message,
                                modifier = Modifier.padding(16.dp),
                                color = Color.Red
                            )
                        }
                    }

                    is MerenderoListUiState.Success -> {

                        val merenderos =
                            (uiState as MerenderoListUiState.Success)
                                .merenderos

                        val necesidades =
                            merenderos.flatMap { merenderoWithNeeds ->
                                merenderoWithNeeds.needs.map { need ->
                                    merenderoWithNeeds.merendero to need
                                }
                            }

                        items(necesidades) { (merendero, need) ->

                            NeedCard(
                                merendero = merendero,
                                need = need,
                                onClick = {
                                    onNeedClick(need.id)
                                },
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 6.dp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NeedCard(
    merendero: Merendero,
    need: NeedItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = !need.isCovered,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (need.isCovered)
                    Color(0xFFF3F4F6)
                else
                    Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    need.urgency?.let {
                        UrgencyBadge(it)

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
                    text =
                        if (need.publishedMinutesAgo < 60)
                            "hace ${need.publishedMinutesAgo} min"
                        else
                            "hace ${need.publishedMinutesAgo / 60} h",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                need.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color(0xFF111827)
            )

            if (need.description.isNotBlank()) {

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    need.description,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 2
                )
            }

            if (need.items.isNotEmpty()) {

                Spacer(modifier = Modifier.height(8.dp))

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

                when {
                    need.donorsOnWay > 0 -> {

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

                    need.isCovered -> {

                        Text(
                            "Ya no necesita donaciones",
                            fontSize = 12.sp,
                            color = Color(0xFF166534),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
