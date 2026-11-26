package com.globalcampus.support.ui.navigator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.globalcampus.support.ui.components.SearchField
import com.globalcampus.support.ui.theme.AccentTeal
import com.globalcampus.support.ui.theme.PrimaryNavy
import com.globalcampus.support.ui.theme.SecondaryRoyalBlue

data class CampusBlock(
    val name: String,
    val code: String,
    val category: String,
    val description: String,
    val floorInfo: String,
    val timings: String,
    val posX: Float,
    val posY: Float,
    val routeSteps: List<String>
)

val LPU_BLOCKS = listOf(
    CampusBlock(
        "Division of International Affairs (DIA)", "Block 32", "Administration",
        "FRRO Registration, Visa Extensions, International Student Help Desk Counter 4.",
        "Ground Floor", "9:00 AM - 5:00 PM", 0.3f, 0.4f,
        listOf("Start at LPU Main Gate 1", "Walk straight along Uni-Mall Road (200m)", "Turn left at Central Circle towards Block 32", "Arrive at Block 32 DIA Counter 4")
    ),
    CampusBlock(
        "Uni-Hospital & Emergency", "Block 27", "Healthcare",
        "24x7 Emergency OPD, Pharmacy, Dental Clinic & Ambulance Counter.",
        "Ground Floor", "24 Hours Open", 0.2f, 0.25f,
        listOf("Start at LPU Main Gate 1", "Turn right at Security Post 1", "Walk 150m along Hospital Avenue", "Arrive at Block 27 Uni-Hospital Main OPD")
    ),
    CampusBlock(
        "Uni-Mall & Commercial Hub", "Uni-Mall", "Shopping & Dining",
        "HDFC/SBI Banks, Airtel/Jio Kiosks, Supermarket, International Food Court (3rd Floor).",
        "Floors 1-4", "9:00 AM - 9:00 PM", 0.5f, 0.45f,
        listOf("Start at LPU Main Gate 1", "Walk straight on Main Boulevard (300m)", "Arrive at Uni-Mall Central Entrance")
    ),
    CampusBlock(
        "Central University Library", "Block 14", "Academics",
        "Digital Library, Quiet Reading Halls, Research Desks & Printing Services.",
        "Floors 1-5", "8:00 AM - 11:00 PM", 0.7f, 0.35f,
        listOf("Start at Uni-Mall", "Walk east past Academic Block 34 (250m)", "Arrive at Central Library Block 14")
    ),
    CampusBlock(
        "Computer Science & Engineering", "Block 34", "Academics",
        "CSE Labs, AI/ML Research Centers, Lecture Halls & Faculty Cabins.",
        "Floors 1-6", "8:30 AM - 5:30 PM", 0.65f, 0.6f,
        listOf("Start at Uni-Mall", "Take South Boulevard towards Academic Row", "Arrive at Block 34 CSE Entrance")
    ),
    CampusBlock(
        "Unipolis Grand Auditorium", "Unipolis", "Events",
        "Major cultural events, international fests, convocations & celebrity concerts.",
        "Main Stage", "Event Dependent", 0.45f, 0.75f,
        listOf("Start at Main Gate", "Follow Central Promenade towards Sports Ground", "Arrive at Unipolis Stage Gate")
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusNavigatorScreen(onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var selectedBlock by remember { mutableStateOf<CampusBlock?>(null) }
    var mapMode by remember { mutableStateOf("Satellite") } // Satellite | Hybrid | Standard

    val filteredBlocks = LPU_BLOCKS.filter {
        query.isBlank() || it.name.contains(query, true) || it.code.contains(query, true) || it.category.contains(query, true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🗺️ LPU Satellite Campus Navigator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        mapMode = when (mapMode) {
                            "Satellite" -> "Hybrid"
                            "Hybrid" -> "Standard"
                            else -> "Satellite"
                        }
                    }) {
                        Icon(Icons.Default.Satellite, "Map Layer", tint = SecondaryRoyalBlue)
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            SearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Search Block 32, Uni-Hospital, Uni-Mall..."
            )

            Spacer(Modifier.height(12.dp))

            // Satellite Map Canvas Container
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = if (mapMode == "Standard") Color(0xFF1E293B) else Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val lineColor = SecondaryRoyalBlue
                    val accentColor = AccentTeal

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Draw Grid Lines (Satellite Map Grid)
                        val gridStep = 35.dp.toPx()
                        var x = 0f
                        while (x < w) {
                            drawLine(Color.White.copy(0.12f), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                            x += gridStep
                        }
                        var y = 0f
                        while (y < h) {
                            drawLine(Color.White.copy(0.12f), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                            y += gridStep
                        }

                        // Draw Main Gate Pin
                        val gateX = w * 0.1f
                        val gateY = h * 0.5f
                        drawCircle(Color.White, radius = 9f, center = Offset(gateX, gateY))

                        // Draw Route Line to Selected Block
                        selectedBlock?.let { block ->
                            val targetX = w * block.posX
                            val targetY = h * block.posY
                            drawLine(
                                color = accentColor,
                                start = Offset(gateX, gateY),
                                end = Offset(targetX, targetY),
                                strokeWidth = 6f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                            )
                        }

                        // Draw Block Pins
                        LPU_BLOCKS.forEach { block ->
                            val pinX = w * block.posX
                            val pinY = h * block.posY
                            val isSelected = selectedBlock?.code == block.code
                            drawCircle(
                                color = if (isSelected) accentColor else SecondaryRoyalBlue,
                                radius = if (isSelected) 15f else 10f,
                                center = Offset(pinX, pinY)
                            )
                        }
                    }

                    // Map Pins Click Overlay
                    LPU_BLOCKS.forEach { block ->
                        val isSelected = selectedBlock?.code == block.code
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            Surface(
                                color = if (isSelected) AccentTeal else Color.White,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset(x = (block.posX * 260).dp, y = (block.posY * 120).dp)
                                    .clickable { selectedBlock = block }
                            ) {
                                Text(
                                    block.code,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else PrimaryNavy,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        color = Color.Black.copy(0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                    ) {
                        Text("📡 $mapMode Layer • Tap pins for directions", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Selected Route Steps Modal Sheet Card
            selectedBlock?.let { block ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Navigation, null, tint = PrimaryNavy)
                            Spacer(Modifier.width(8.dp))
                            Text("Turn-by-Turn Route to ${block.code}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryNavy)
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { selectedBlock = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, "Close Route")
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        block.routeSteps.forEachIndexed { idx, step ->
                            Text("${idx + 1}. $step", fontSize = 12.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Campus Blocks List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredBlocks) { b ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedBlock = b }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = SecondaryRoyalBlue, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(b.code, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SecondaryRoyalBlue)
                                Spacer(Modifier.weight(1f))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(b.category, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(b.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(b.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f), lineHeight = 18.sp)

                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍 ${b.floorInfo}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.weight(1f))
                                Button(
                                    onClick = { selectedBlock = b },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryRoyalBlue),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.Directions, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Route Guide", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
