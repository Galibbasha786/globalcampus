package com.globalcampus.support.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.globalcampus.support.R
import com.globalcampus.support.data.repository.AuthRepository
import com.globalcampus.support.ui.components.SearchField
import com.globalcampus.support.ui.components.SectionHeader
import com.globalcampus.support.ui.components.StudentAvatar
import kotlinx.coroutines.delay
import kotlin.math.sin

// --- LOGO THEME COLORS ---
val LogoOrange = Color(0xFFE87A00)
val LogoBlue = Color(0xFF0077B6)
val LogoGreen = Color(0xFF2D9F4D)
val LogoYellow = Color(0xFFF9C80E)
val LogoRed = Color(0xFFD62828)

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onOpenInfoHub: () -> Unit,
    onOpenCommunity: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenVisaTools: () -> Unit,
    onOpenPhrasebook: () -> Unit,
    onOpenPlanner: () -> Unit = {},
    onOpenBuddy: () -> Unit = {},
    onOpenBudget: () -> Unit = {},
    onOpenEvents: () -> Unit = {},
    onOpenNavigator: () -> Unit = {},
    onOpenDocExplainer: () -> Unit = {},
    onOpenLostFound: () -> Unit = {},
    onOpenAcademic: () -> Unit = {}
) {
    val auth = AuthRepository()
    val user = auth.currentUser
    var searchQuery by remember { mutableStateOf("") }

    // Master infinite transition for ambient motion
    val infiniteTransition = rememberInfiniteTransition(label = "masterTransition")

    // Floating offsets for cards (staggered)
    val float1 by infiniteTransition.animateFloat(
        initialValue = -2f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "float1"
    )
    val float2 by infiniteTransition.animateFloat(
        initialValue = 2f, targetValue = -2f,
        animationSpec = infiniteRepeatable(tween(3400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "float2"
    )

    // Animated Staggered Entrance Helper
    @Composable
    fun AnimatedEntrance(
        delayMillis: Int = 0,
        content: @Composable () -> Unit
    ) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(delayMillis.toLong())
            visible = true
        }
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                    ) + scaleIn(initialScale = 0.9f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        ) {
            content()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LogoBlue.copy(alpha = 0.05f),
                        LogoOrange.copy(alpha = 0.03f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Floating Ambient Gradient Orbs
        val orbShift by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 30f,
            animationSpec = infiniteRepeatable(tween(4000, easing = EaseInOutSine), RepeatMode.Reverse),
            label = "orbShift"
        )

        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = 180.dp, y = (-50 + orbShift).dp)
                .background(
                    Brush.radialGradient(listOf(LogoOrange.copy(0.12f), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-80).dp, y = (300 - orbShift).dp)
                .background(
                    Brush.radialGradient(listOf(LogoBlue.copy(0.12f), Color.Transparent)),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
        Spacer(Modifier.height(16.dp))

        // ===== HEADER: Animated Logo & Avatar =====
        AnimatedEntrance(delayMillis = 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 3D Floating Logo with Double Glow
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(64.dp)
                    ) {
                        // Outer glow (Orange)
                        val glowAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.2f, targetValue = 0.6f,
                            animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
                            label = "glow"
                        )
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(LogoOrange.copy(alpha = glowAlpha), Color.Transparent)
                                    ),
                                    shape = CircleShape
                                )
                        )
                        // Inner logo image
                        val logoOffsetY by infiniteTransition.animateFloat(
                            initialValue = -3f, targetValue = 3f,
                            animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOutSine), RepeatMode.Reverse),
                            label = "logoFloat"
                        )
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "GlobalCampus Logo",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .graphicsLayer { translationY = logoOffsetY },
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = "Hello, Student",
                            transitionSpec = {
                                (fadeIn(tween(800)) + slideInHorizontally { -it / 2 }) togetherWith
                                        fadeOut(tween(400))
                            },
                            label = "greeting"
                        ) { text ->
                            Text(
                                text = text,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = user?.email ?: "LPU International Hub",
                            fontSize = 13.sp,
                            color = LogoBlue,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }

                // Pulsing online avatar
                val avatarScale by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
                    label = "avatarScale"
                )
                Box(
                    modifier = Modifier
                        .scale(avatarScale)
                        .shadow(12.dp, CircleShape, spotColor = LogoOrange)
                ) {
                    StudentAvatar(
                        avatarUrl = "",
                        name = user?.email ?: "Student",
                        size = 50.dp,
                        showOnlineStatus = true
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ===== SEARCH =====
        AnimatedEntrance(delayMillis = 100) {
            SearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search FRRO, campus blocks, posts, guides..."
            )
        }

        Spacer(Modifier.height(24.dp))

        // ===== HERO SECTION: Infinite Drifting Gradient =====
        AnimatedEntrance(delayMillis = 200) {
            val heroShift by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
                label = "heroShift"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(LogoOrange, LogoRed, LogoBlue, LogoGreen),
                            start = Offset(heroShift * 1000f, 0f),
                            end = Offset(heroShift * 1000f + 500f, 500f)
                        )
                    )
                    .padding(20.dp)
            ) {
                // Floating background circles
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .offset(x = 200.dp, y = (sin(heroShift * 2 * Math.PI) * 20).dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                )

                Column {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "INTERNATIONAL AFFAIRS",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Welcome to LPU GlobalCampus",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Your international student companion. Access FRRO visa guidelines, campus community, 24/7 AI assistance & support.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ===== SLIDING IMAGE CAROUSEL (l1, l2, l3) =====
        AnimatedEntrance(delayMillis = 240) {
            DashboardImageCarousel()
        }

        Spacer(Modifier.height(24.dp))

        AnimatedEntrance(delayMillis = 280) {
            SectionHeader(
                title = "International Student Hub",
                subtitle = "Smart AI tools & campus services"
            )
        }

        Spacer(Modifier.height(12.dp))

        // ===== ANIMATED FEATURE GRID =====
        // Using the floating offsets to create a continuous ocean-wave effect

        AnimatedEntrance(delayMillis = 340) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                HomeFeatureCard("AI Arrival Planner", "1st Day, Week & Month Tasks", Icons.Default.FlightLand, LogoOrange, Modifier.weight(1f).offset(y = float1.dp), onOpenPlanner)
                HomeFeatureCard("Buddy Matcher", "Find Country & Course Peers", Icons.Default.People, LogoBlue, Modifier.weight(1f).offset(y = float2.dp), onOpenBuddy)
            }
        }

        Spacer(Modifier.height(14.dp))

        AnimatedEntrance(delayMillis = 400) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                HomeFeatureCard("Budget Planner", "Track Monthly INR Expenses", Icons.Default.AccountBalanceWallet, LogoGreen, Modifier.weight(1f).offset(y = float2.dp), onOpenBudget)
                HomeFeatureCard("Campus Events", "Cultural Fests & RSVPs", Icons.Default.Event, LogoYellow, Modifier.weight(1f).offset(y = float1.dp), onOpenEvents)
            }
        }

        Spacer(Modifier.height(14.dp))

        AnimatedEntrance(delayMillis = 460) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                HomeFeatureCard("Campus Navigator", "Block Directory & Timings", Icons.Default.Map, LogoBlue, Modifier.weight(1f).offset(y = float1.dp), onOpenNavigator)
                HomeFeatureCard("AI Doc Explainer", "Plain-English Document Summaries", Icons.Default.AutoAwesome, LogoRed, Modifier.weight(1f).offset(y = float2.dp), onOpenDocExplainer)
            }
        }

        Spacer(Modifier.height(14.dp))

        AnimatedEntrance(delayMillis = 520) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                HomeFeatureCard("Lost & Found", "Report & Search Belongings", Icons.Default.Search, LogoOrange, Modifier.weight(1f).offset(y = float2.dp), onOpenLostFound)
                HomeFeatureCard("Academic Toolkit", "Study Deadlines & Reminders", Icons.Default.School, LogoGreen, Modifier.weight(1f).offset(y = float1.dp), onOpenAcademic)
            }
        }

        Spacer(Modifier.height(14.dp))

        AnimatedEntrance(delayMillis = 580) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                HomeFeatureCard("Community Feed", "Discussions & Photos", Icons.Default.Groups, LogoBlue, Modifier.weight(1f).offset(y = float1.dp), onOpenCommunity)
                HomeFeatureCard("Help Center", "Support & Visa Requests", Icons.Default.Support, LogoRed, Modifier.weight(1f).offset(y = float2.dp), onOpenHelp)
            }
        }

        Spacer(Modifier.height(28.dp))

        // ===== AI ASSISTANT BANNER =====
        AnimatedEntrance(delayMillis = 640) {
            val shimmer by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
                label = "shimmer"
            )
            val botBounce by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = -6f,
                animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "botBounce"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenChat() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LogoBlue.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Shimmer overlay
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent),
                                    start = Offset(shimmer * 1200f, 0f),
                                    end = Offset(shimmer * 1200f + 400f, 400f)
                                )
                            )
                    )
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bouncing Bot Icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .offset(y = botBounce.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(LogoOrange, LogoRed))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ask GlobalBot AI", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LogoBlue)
                            Text(
                                "Instant guidance on visa, hostels, exams & campus life",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = LogoOrange)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ===== EMERGENCY CONTACTS =====
        AnimatedEntrance(delayMillis = 700) {
            val phonePulse by infiniteTransition.animateFloat(
                initialValue = 0.9f, targetValue = 1.2f,
                animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "phonePulse"
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = LogoRed.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LogoRed.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .scale(phonePulse)
                                .background(LogoRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhoneInTalk, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("24x7 Campus Emergency Support", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = LogoRed)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "• DIA Desk: +91-1824-517000\n• Uni-Hospital Ambulance: 108 / Ext: 3333\n• Campus Security: Ext: 1111",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}
}

@Composable
private fun HomeFeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Bouncy press animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cardScale"
    )

    // Icon rotation on press
    val iconRotation by animateFloatAsState(
        targetValue = if (isPressed) 15f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessLow),
        label = "iconRot"
    )

    // Continuous soft glow for the icon
    val infiniteTransition = rememberInfiniteTransition(label = "cardGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glowScale"
    )

    Card(
        modifier = modifier
            .height(135.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Animated Icon Container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(glowScale)
                    .rotate(iconRotation)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
            }

            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                    maxLines = 2,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun DashboardImageCarousel() {
    val banners = listOf(
        Triple(R.drawable.l1, "LPU One World Cultural Fest", "Explore 50+ international cultural pavilions"),
        Triple(R.drawable.l2, "Division of International Affairs", "Block 32 FRRO desk & student support"),
        Triple(R.drawable.l3, "Uni-Mall & Student Life", "Banks, international dining & shopping")
    )
    val pagerState = androidx.compose.foundation.pager.rememberPagerState { banners.size }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3500)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
        ) { page ->
            val (imgRes, title, subtitle) = banners[page]
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = imgRes),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(0.75f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(subtitle, color = Color.White.copy(0.85f), fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(banners.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = if (isSelected) 20.dp else 8.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) LogoOrange else Color.LightGray.copy(0.5f))
                )
            }
        }
    }
}
