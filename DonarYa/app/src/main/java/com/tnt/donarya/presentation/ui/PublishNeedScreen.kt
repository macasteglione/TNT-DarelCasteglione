package com.tnt.donarya.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.tnt.donarya.domain.model.NeedType
import com.tnt.donarya.domain.model.UrgencyLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishNeedScreen(
    onBack: () -> Unit,
    onPublish: (type: NeedType, urgency: UrgencyLevel, description: String, items: List<String>, whatsapp: String) -> Unit,
    initialWhatsapp: String = ""
) {
    var selectedType by remember { mutableStateOf<NeedType?>(null) }
    var selectedUrgency by remember { mutableStateOf<UrgencyLevel?>(null) }
    var description by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(emptyList<String>()) }
    var whatsapp by remember { mutableStateOf(initialWhatsapp) }
    var newItem by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Detalle de la necesidad", fontSize = 16.sp, color = Color(0xFF374151))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val type = selectedType ?: return@TextButton
                        val urgency = selectedUrgency ?: return@TextButton
                        onPublish(type, urgency, description, items, whatsapp)
                    }) {
                        Text("Guardar", color = Color(0xFF40916C), fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Need type
            FormSection(title = "Tipo de necesidad") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(NeedType.values()) { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFF1B4332) else Color.White)
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent else Color(0xFFE5E7EB),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedType = type }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "${type.emoji} ${type.label}",
                                color = if (isSelected) Color.White else Color(0xFF374151),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Urgency
            FormSection(title = "Nivel de urgencia") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    UrgencyOption(
                        UrgencyLevel.URGENTE,
                        "🔴",
                        "Necesito\nahora mismo",
                        selectedUrgency
                    ) { selectedUrgency = it }
                    UrgencyOption(
                        UrgencyLevel.ESTA_SEMANA,
                        "🟡",
                        "Esta\nsemana",
                        selectedUrgency
                    ) { selectedUrgency = it }
                    UrgencyOption(
                        UrgencyLevel.SIN_APURO,
                        "🟢",
                        "Sin\napuro",
                        selectedUrgency
                    ) { selectedUrgency = it }
                }
            }

            // Description
            FormSection(title = "Descripción") {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    placeholder = { Text("Describí qué necesitás y para cuántas personas...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF40916C),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    ),
                    trailingIcon = {
                        Text(
                            "${description.length}/150",
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                )
            }

            // Items
            FormSection(
                title = "¿Qué artículos específicos?",
                subtitle = "Agregá cada ítem por separado"
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items) { item ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFD8F3DC))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    item,
                                    fontSize = 13.sp,
                                    color = Color(0xFF2D6A4F),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Eliminar",
                                    tint = Color(0xFF40916C),
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { items = items.filter { it != item } }
                                )
                            }
                        }
                    }
                    item {
                        // Add button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFF3F4F6))
                                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(20.dp))
                                .clickable {
                                    if (newItem.isNotEmpty()) {
                                        items = items + newItem; newItem = ""
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("+ Agregar", fontSize = 13.sp, color = Color(0xFF6B7280))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newItem,
                    onValueChange = { newItem = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Escribí un artículo...") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF40916C),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    ),
                    trailingIcon = {
                        TextButton(onClick = {
                            if (newItem.isNotEmpty()) {
                                items = items + newItem; newItem = ""
                            }
                        }) {
                            Text(
                                "+",
                                color = Color(0xFF40916C),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }
                )
            }

            // WhatsApp
            FormSection(title = "WhatsApp de contacto") {
                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color(0xFF25D366)
                        )
                    },
                    placeholder = { Text("+54 9 ...") },
                    shape = RoundedCornerShape(12.dp),
                    supportingText = { Text("Los donantes podrán contactarte") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF40916C),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
            }

            // Publish button
            Button(
                onClick = {
                    val type = selectedType ?: return@Button
                    val urgency = selectedUrgency ?: return@Button
                    onPublish(type, urgency, description, items, whatsapp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF40916C)),
                shape = RoundedCornerShape(14.dp),
                enabled = selectedType != null && selectedUrgency != null && description.isNotBlank()
            ) {
                Text("Publicar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FormSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF111827))
        subtitle?.let {
            Text(
                it,
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun RowScope.UrgencyOption(
    urgency: UrgencyLevel,
    emoji: String,
    label: String,
    selected: UrgencyLevel?,
    onSelect: (UrgencyLevel) -> Unit
) {
    val isSelected = selected == urgency
    val color = when (urgency) {
        UrgencyLevel.URGENTE -> Color(0xFFE63946)
        UrgencyLevel.ESTA_SEMANA -> Color(0xFFF4A261)
        UrgencyLevel.SIN_APURO -> Color(0xFF52B788)
    }
    val bgColor = if (isSelected) color.copy(alpha = 0.12f) else Color.White
    val borderColor = if (isSelected) color else Color(0xFFE5E7EB)

    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onSelect(urgency) }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                fontSize = 11.sp,
                color = if (isSelected) color else Color(0xFF6B7280),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )
        }
    }
}
