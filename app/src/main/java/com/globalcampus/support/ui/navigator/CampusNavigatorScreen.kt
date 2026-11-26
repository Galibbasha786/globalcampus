package com.globalcampus.support.ui.navigator

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.globalcampus.support.ui.components.SearchField
import com.globalcampus.support.ui.theme.AccentTeal
import com.globalcampus.support.ui.theme.PrimaryNavy
import com.globalcampus.support.ui.theme.SecondaryRoyalBlue
import kotlin.math.cos
import kotlin.math.sin

data class CampusBlock(
    val name: String,
    val code: String,
    val category: String,
    val description: String,
    val floorInfo: String,
    val timings: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Int,
    val walkingMinutes: Int,
    val posX: Float, // Relative 0f..1f for Tactical Radar HUD
    val posY: Float,
    val routeSteps: List<String>
)

val LPU_CAMPUS_BLOCKS = listOf(
    CampusBlock(
        name = "Division of International Affairs (DIA)",
        code = "Block 32",
        category = "Administration",
        description = "FRRO Registration, Visa Extensions, International Student Help Desk (Counters 1-6), Bonafide verification & C-Form endorsement.",
        floorInfo = "Ground Floor (Counters 1-6) & 1st Floor Admin",
        timings = "9:00 AM - 5:30 PM (Mon-Sat)",
        latitude = 31.2542,
        longitude = 75.7038,
        distanceMeters = 350,
        walkingMinutes = 4,
        posX = 0.32f,
        posY = 0.42f,
        routeSteps = listOf(
            "Start at LPU Main Gate 1 (NH-44 Entry)",
            "Walk straight along Uni-Mall Boulevard (200m)",
            "Turn left at the Central Fountain Circle towards Block 32",
            "Arrive at Block 32 DIA International Desk Counter 4"
        )
    ),
    CampusBlock(
        name = "Uni-Hospital & Emergency Center",
        code = "Block 27",
        category = "Healthcare",
        description = "24x7 Emergency OPD, In-house Pharmacy, Ambulance Desk, Dental & Pathology Lab.",
        floorInfo = "Ground Floor OPD, 1st Floor Speciality Clinics",
        timings = "24 Hours Open (Emergency 24x7)",
        latitude = 31.2561,
        longitude = 75.7015,
        distanceMeters = 220,
        walkingMinutes = 3,
        posX = 0.20f,
        posY = 0.26f,
        routeSteps = listOf(
            "Start at LPU Main Gate 1",
            "Turn right at Security Post 1 onto Hospital Avenue",
            "Walk 150m past the Ambulance Bay",
            "Arrive at Block 27 Uni-Hospital Main Emergency Reception"
        )
    ),
    CampusBlock(
        name = "Uni-Mall & Commercial Center",
        code = "Uni-Mall",
        category = "Dining & Banking",
        description = "HDFC & SBI Branches/ATMs, Airtel/Jio 5G SIM Kiosks, Supermarket & International Food Court.",
        floorInfo = "Floors 1-4 (Food Court on Level 3)",
        timings = "8:30 AM - 10:00 PM Daily",
        latitude = 31.2536,
        longitude = 75.7032,
        distanceMeters = 320,
        walkingMinutes = 4,
        posX = 0.50f,
        posY = 0.46f,
        routeSteps = listOf(
            "Start at LPU Main Gate 1",
            "Proceed south on Central Main Boulevard (300m)",
            "Cross the pedestrian walkway opposite Block 30",
            "Enter Uni-Mall Central Glass Entrance"
        )
    ),
    CampusBlock(
        name = "Computer Science & Engineering (CSE)",
        code = "Block 34",
        category = "Academics",
        description = "CSE/IT Department, AI & Robotics Centers, Apple iOS Development Lab, Cloud Computing & Faculty Cabins.",
        floorInfo = "Floors 1-7 (AI Labs on Floor 4)",
        timings = "8:00 AM - 5:30 PM",
        latitude = 31.2558,
        longitude = 75.7056,
        distanceMeters = 580,
        walkingMinutes = 7,
        posX = 0.65f,
        posY = 0.58f,
        routeSteps = listOf(
            "Start at Uni-Mall Central Exit",
            "Walk east down Academic Avenue past Block 33",
            "Continue straight towards Block 34 Tech Atrium",
            "Enter Block 34 North Wing"
        )
    ),
    CampusBlock(
        name = "Central University Library",
        code = "Block 14",
        category = "Academics",
        description = "Multi-storey Air-conditioned Digital Library, 1.5M+ Books & Research Journals, Quiet Reading Halls.",
        floorInfo = "Floors 1-5 (Reference Section Level 3)",
        timings = "8:00 AM - 11:30 PM",
        latitude = 31.2519,
        longitude = 75.7061,
        distanceMeters = 750,
        walkingMinutes = 9,
        posX = 0.72f,
        posY = 0.36f,
        routeSteps = listOf(
            "Start at Uni-Mall",
            "Walk southeast along Library Garden Walkway (400m)",
            "Turn left past the Management Block lawn",
            "Arrive at Central Library Block 14 Turnstiles"
        )
    ),
    CampusBlock(
        name = "Unipolis Grand Auditorium & Arena",
        code = "Unipolis",
        category = "Events",
        description = "10,000+ capacity open-air amphitheatre, One World Cultural Fest arena, Convocations & Global fests.",
        floorInfo = "Ground Amphitheatre & VIP Stage",
        timings = "Event Dependent (Fests & Convocations)",
        latitude = 31.2526,
        longitude = 75.7025,
        distanceMeters = 480,
        walkingMinutes = 6,
        posX = 0.44f,
        posY = 0.76f,
        routeSteps = listOf(
            "Start at LPU Main Gate 1",
            "Walk along Main Boulevard towards the central sports corridor",
            "Veer right at the Stadium Circle",
            "Enter Unipolis Grand Gate 2"
        )
    ),
    CampusBlock(
        name = "Shanti Devi Mittal Indoor Stadium",
        code = "Sports Complex",
        category = "Sports & Fitness",
        description = "Olympic-size Swimming Pool, Indoor Badminton Courts, Basketball, Squash & High-Performance Gymnasium.",
        floorInfo = "Ground Floor Courts, 1st Floor Fitness Hub",
        timings = "6:00 AM - 9:00 PM Daily",
        latitude = 31.2512,
        longitude = 75.7010,
        distanceMeters = 650,
        walkingMinutes = 8,
        posX = 0.30f,
        posY = 0.85f,
        routeSteps = listOf(
            "Start at Unipolis South Arena",
            "Walk southwest towards the Olympic Sports Complex",
            "Enter through Main Sports Turnstile Desk"
        )
    ),
    CampusBlock(
        name = "Boys Hostel Hub (BH-1 to BH-8)",
        code = "BH Complex",
        category = "Hostels",
        description = "Multi-tower Boys Hostel complex, Central Warden Office, Night Mess, Gym & Laundry Services.",
        floorInfo = "Towers BH-1 through BH-8",
        timings = "Curfew: 9:00 PM (Hostel Entry)",
        latitude = 31.2575,
        longitude = 75.7080,
        distanceMeters = 850,
        walkingMinutes = 10,
        posX = 0.85f,
        posY = 0.20f,
        routeSteps = listOf(
            "Start at Block 34 CSE",
            "Take North Residential Bypass Road (300m)",
            "Arrive at BH-1 Central Warden Checkpost"
        )
    ),
    CampusBlock(
        name = "Girls Hostel Hub (GH-1 to GH-6)",
        code = "GH Complex",
        category = "Hostels",
        description = "Secure residential complex for female students, Biometric Turnstiles, Resident Warden Office & Dining Hall.",
        floorInfo = "Towers GH-1 through GH-6",
        timings = "Curfew: 7:30 PM (Biometric Turnstiles)",
        latitude = 31.2505,
        longitude = 75.7040,
        distanceMeters = 700,
        walkingMinutes = 9,
        posX = 0.55f,
        posY = 0.88f,
        routeSteps = listOf(
            "Start at Uni-Mall South Exit",
            "Proceed south down Girls Hostel Avenue (350m)",
            "Arrive at GH Main Gate Security Station"
        )
    )
)

val CATEGORIES = listOf("All", "Administration", "Academics", "Healthcare", "Dining & Banking", "Events", "Hostels", "Sports & Fitness")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusNavigatorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedBlock by remember { mutableStateOf<CampusBlock?>(LPU_CAMPUS_BLOCKS.first()) }
    // Map modes: "Satellite" (Live Satellite Map), "Hybrid" (Satellite with labels), "Tactical Radar" (Compose radar HUD)
    var mapMode by remember { mutableStateOf("Satellite") }
    var isMapExpanded by remember { mutableStateOf(false) }

    // Radar rotation animation for Tactical Radar mode
    val infiniteTransition = rememberInfiniteTransition(label = "satelliteRadar")
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarSweep"
    )
    val beaconPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconPulse"
    )

    val filteredBlocks = LPU_CAMPUS_BLOCKS.filter { block ->
        val matchesCategory = selectedCategory == "All" || block.category.equals(selectedCategory, ignoreCase = true)
        val matchesQuery = query.isBlank() ||
                block.name.contains(query, ignoreCase = true) ||
                block.code.contains(query, ignoreCase = true) ||
                block.category.contains(query, ignoreCase = true) ||
                block.description.contains(query, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛰️ LPU Satellite Navigator", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }
                        Text(
                            "GPS Fix: 31.2536° N, 75.7037° E • LPU Campus",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Mode toggle pill
                    Surface(
                        color = SecondaryRoyalBlue.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable {
                                mapMode = when (mapMode) {
                                    "Satellite" -> "Hybrid"
                                    "Hybrid" -> "Tactical Radar"
                                    else -> "Satellite"
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Satellite, null, tint = SecondaryRoyalBlue, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(mapMode, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SecondaryRoyalBlue)
                        }
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
        ) {
            // Satellite Telemetry Status Bar
            Surface(
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "SATELLITE LOCK ACTIVE",
                            color = Color(0xFF10B981),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        "18 SVs • Alt 245m • NavIC/GPS",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Satellite Map View Port (Embedded Satellite Map or Tactical Radar)
            Card(
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B132B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isMapExpanded) 340.dp else 220.dp)
                    .animateContentSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (mapMode == "Tactical Radar") {
                        // Tactical Radar HUD Canvas
                        TacticalRadarCanvas(
                            selectedBlock = selectedBlock,
                            radarAngle = radarAngle,
                            beaconPulse = beaconPulse,
                            onSelectBlock = { selectedBlock = it }
                        )
                    } else {
                        // Live Google Satellite / Hybrid Map Embedded WebView
                        LiveSatelliteWebView(
                            selectedBlock = selectedBlock,
                            isHybrid = mapMode == "Hybrid"
                        )
                    }

                    // Map Overlay Controls
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Expand/Collapse Map
                        SmallFloatingActionButton(
                            onClick = { isMapExpanded = !isMapExpanded },
                            containerColor = Color.Black.copy(alpha = 0.75f),
                            contentColor = Color.White,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                if (isMapExpanded) Icons.Default.CloseFullscreen else Icons.Default.OpenInFull,
                                "Toggle Size",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Re-center on Main Gate 1
                        SmallFloatingActionButton(
                            onClick = { selectedBlock = LPU_CAMPUS_BLOCKS.first() },
                            containerColor = Color.Black.copy(alpha = 0.75f),
                            contentColor = Color.White,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, "Recenter Gate 1", modifier = Modifier.size(16.dp))
                        }
                    }

                    // Layer Badge & Target GPS Coordinates
                    Surface(
                        color = Color.Black.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Satellite, null, tint = AccentTeal, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                selectedBlock?.let {
                                    "${it.code} [${String.format("%.4f", it.latitude)}°N, ${String.format("%.4f", it.longitude)}°E]"
                                } ?: "LPU Campus Datum: 31.2536°N, 75.7037°E",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Main Content Area: Search, Filters, and Blocks List
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(10.dp))

                SearchField(
                    query = query,
                    onQueryChange = { query = it },
                    placeholder = "Search Block 32, Uni-Hospital, CSE, Library..."
                )

                Spacer(Modifier.height(8.dp))

                // Category Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CATEGORIES.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SecondaryRoyalBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Selected Target Card with Satellite Actions
                selectedBlock?.let { block ->
                    SatelliteTargetCard(
                        block = block,
                        context = context,
                        onClose = { selectedBlock = null }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                // Campus Blocks List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(filteredBlocks) { b ->
                        CampusBlockCard(
                            block = b,
                            isSelected = selectedBlock?.code == b.code,
                            onSelect = { selectedBlock = b },
                            onNavigateGoogleMaps = { launchGoogleSatelliteMaps(context, b) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Embedded Live Satellite Map WebView powered by Google Satellite Maps.
 * Loads high-resolution satellite imagery tiles directly for Lovely Professional University.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LiveSatelliteWebView(
    selectedBlock: CampusBlock?,
    isHybrid: Boolean
) {
    val targetLat = selectedBlock?.latitude ?: 31.2536
    val targetLng = selectedBlock?.longitude ?: 75.7037
    // Google Maps embed URL parameter: 'k' = Satellite, 'h' = Hybrid (Satellite + Road/Building Labels)
    val mapType = if (isHybrid) "h" else "k"
    val zoom = if (selectedBlock != null) "18" else "16"
    val mapEmbedUrl = "https://maps.google.com/maps?q=$targetLat,$targetLng&t=$mapType&z=$zoom&output=embed"

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    builtInZoomControls = true
                    displayZoomControls = false
                }
                loadUrl(mapEmbedUrl)
            }
        },
        update = { webView ->
            if (webView.url != mapEmbedUrl) {
                webView.loadUrl(mapEmbedUrl)
            }
        }
    )
}

/**
 * High-Tech Tactical Satellite Radar HUD Canvas.
 * Renders satellite coordinates, rotating radar sweep beam, campus roads, and block beacons.
 */
@Composable
fun TacticalRadarCanvas(
    selectedBlock: CampusBlock?,
    radarAngle: Float,
    beaconPulse: Float,
    onSelectBlock: (CampusBlock) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Satellite Earth Grid Lines
            val gridStep = 32.dp.toPx()
            var x = 0f
            while (x < w) {
                drawLine(Color(0xFF1E293B).copy(0.7f), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                x += gridStep
            }
            var y = 0f
            while (y < h) {
                drawLine(Color(0xFF1E293B).copy(0.7f), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                y += gridStep
            }

            // Radar Concentric Range Rings from Campus Center
            val centerX = w * 0.45f
            val centerY = h * 0.5f
            val radii = listOf(w * 0.2f, w * 0.38f, w * 0.55f)
            radii.forEach { r ->
                drawCircle(
                    color = Color(0xFF0284C7).copy(0.25f),
                    radius = r,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                )
            }

            // Radar Rotating Sweep Beam
            val rad = Math.toRadians(radarAngle.toDouble())
            val sweepX = centerX + (w * 0.6f) * cos(rad).toFloat()
            val sweepY = centerY + (w * 0.6f) * sin(rad).toFloat()
            drawLine(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00F5D4).copy(0.6f), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = w * 0.6f
                ),
                start = Offset(centerX, centerY),
                end = Offset(sweepX, sweepY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // Campus Main Gate 1 Origin Datum
            val gateX = w * 0.12f
            val gateY = h * 0.48f
            drawCircle(Color.White, radius = 9f, center = Offset(gateX, gateY))
            drawCircle(Color(0xFF38BDF8), radius = 14f, center = Offset(gateX, gateY), style = Stroke(2f))

            // Turn-by-Turn GPS Route Line to Target
            selectedBlock?.let { block ->
                val targetX = w * block.posX
                val targetY = h * block.posY
                drawLine(
                    color = AccentTeal,
                    start = Offset(gateX, gateY),
                    end = Offset(targetX, targetY),
                    strokeWidth = 6f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                )
            }

            // Satellite Block Beacons
            LPU_CAMPUS_BLOCKS.forEach { block ->
                val pinX = w * block.posX
                val pinY = h * block.posY
                val isSelected = selectedBlock?.code == block.code

                if (isSelected) {
                    // Pulsing Outer Radar Ring
                    drawCircle(
                        color = AccentTeal.copy(0.4f),
                        radius = 24f * beaconPulse,
                        center = Offset(pinX, pinY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 12f,
                        center = Offset(pinX, pinY)
                    )
                    drawCircle(
                        color = AccentTeal,
                        radius = 8f,
                        center = Offset(pinX, pinY)
                    )
                } else {
                    drawCircle(
                        color = SecondaryRoyalBlue,
                        radius = 9f,
                        center = Offset(pinX, pinY)
                    )
                }
            }
        }

        // Tap targets for blocks on HUD
        LPU_CAMPUS_BLOCKS.forEach { block ->
            val isSelected = selectedBlock?.code == block.code
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Surface(
                    color = if (isSelected) AccentTeal else Color(0xFF0F172A).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color.White else SecondaryRoyalBlue),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (block.posX * 260).dp, y = (block.posY * 130).dp)
                        .clickable { onSelectBlock(block) }
                ) {
                    Text(
                        block.code,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF0F172A) else Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Satellite Target Intelligence & Turn-by-Turn Navigation Card.
 */
@Composable
fun SatelliteTargetCard(
    block: CampusBlock,
    context: Context,
    onClose: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Satellite, null, tint = SecondaryRoyalBlue)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Satellite Lock: ${block.code}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryNavy
                    )
                    Text(
                        "${block.name} • ${block.distanceMeters}m (~${block.walkingMinutes} min walk)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "Close Route")
                }
            }

            Spacer(Modifier.height(10.dp))

            // Action Buttons Row: Google Satellite & Turn-by-Turn GPS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { launchGoogleSatelliteMaps(context, block) },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryRoyalBlue),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Satellite, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Google Satellite", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = { launchWalkingNavigation(context, block) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("GPS Walk Guide", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = { shareLocationCoordinates(context, block) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Share, "Share GPS", tint = PrimaryNavy)
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = PrimaryNavy.copy(alpha = 0.15f))
            Spacer(Modifier.height(8.dp))

            Text("Turn-by-Turn Walking Route from Main Gate 1:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryNavy)
            Spacer(Modifier.height(4.dp))
            block.routeSteps.forEachIndexed { idx, step ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Text("${idx + 1}. ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondaryRoyalBlue)
                    Text(step, fontSize = 11.sp, lineHeight = 15.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

/**
 * Individual Campus Block Card in Directory List.
 */
@Composable
fun CampusBlockCard(
    block: CampusBlock,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onNavigateGoogleMaps: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(1.5.dp, SecondaryRoyalBlue) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = SecondaryRoyalBlue, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(block.code, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SecondaryRoyalBlue)
                Spacer(Modifier.weight(1f))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(block.category, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(block.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(3.dp))
            Text(
                block.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                lineHeight = 17.sp
            )

            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "🛰️ ${String.format("%.4f", block.latitude)}°N, ${String.format("%.4f", block.longitude)}°E",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
                Text(
                    "• ${block.distanceMeters}m (~${block.walkingMinutes} min)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryRoyalBlue
                )
            }

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📍 ${block.floorInfo}", fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Button(
                    onClick = onNavigateGoogleMaps,
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryRoyalBlue),
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Satellite, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Satellite Route", fontSize = 11.sp)
                }
            }
        }
    }
}

/**
 * Launch Google Maps explicitly in Satellite mode ('t=k') focused on the block's coordinates.
 */
fun launchGoogleSatelliteMaps(context: Context, block: CampusBlock) {
    try {
        val uri = Uri.parse("geo:${block.latitude},${block.longitude}?q=${block.latitude},${block.longitude}(${Uri.encode("LPU " + block.name)})&t=k")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // Fallback to browser satellite URL
        val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${block.latitude},${block.longitude}&basemap=satellite")
        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
    }
}

/**
 * Launch Google Maps walking navigation mode.
 */
fun launchWalkingNavigation(context: Context, block: CampusBlock) {
    try {
        val uri = Uri.parse("google.navigation:q=${block.latitude},${block.longitude}&mode=w")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${block.latitude},${block.longitude}&travelmode=walking")
        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
    }
}

/**
 * Share GPS Coordinates and block location details with peers.
 */
fun shareLocationCoordinates(context: Context, block: CampusBlock) {
    val shareText = "📍 LPU Campus Location: ${block.name} (${block.code})\n" +
            "🛰️ Satellite Coordinates: ${block.latitude}, ${block.longitude}\n" +
            "🏢 Floor: ${block.floorInfo}\n" +
            "🗺️ Google Satellite View: https://maps.google.com/?q=${block.latitude},${block.longitude}&t=k"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share LPU Location"))
}
