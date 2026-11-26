
package com.globalcampus.support.ui.buddy

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.globalcampus.support.data.model.BuddyProfile
import com.globalcampus.support.data.repository.BuddyRepository
import com.globalcampus.support.ui.components.SearchField
import com.globalcampus.support.ui.components.StudentAvatar
import com.globalcampus.support.ui.theme.PrimaryNavy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Brand Colors
val LpuOrange = Color(0xFFF37021)
val GlobeBlue = Color(0xFF1E88E5)

data class BuddyState(
    val loading: Boolean = true,
    val buddies: List<BuddyProfile> = emptyList(),
    val query: String = "",
    val sentRequests: Set<String> = emptySet(),
    val message: String? = null
) {
    val filtered: List<BuddyProfile>
        get() = buddies.filter {
            query.isBlank() ||
            it.name.contains(query, true) ||
            it.regdNo.contains(query, true) ||
            it.country.contains(query, true) ||
            it.course.contains(query, true)
        }
}

class BuddyViewModel : ViewModel() {
    private val repo = BuddyRepository()
    private val _state = MutableStateFlow(BuddyState())
    val state: StateFlow<BuddyState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val res = repo.getBuddyProfiles()
            _state.value = _state.value.copy(loading = false, buddies = res.getOrDefault(emptyList()))
        }
    }

    fun setQuery(q: String) { _state.value = _state.value.copy(query = q) }

    fun sendRequest(buddy: BuddyProfile) {
        viewModelScope.launch {
            repo.sendBuddyRequest(buddy.uid)
            val updated = _state.value.sentRequests + buddy.uid
            _state.value = _state.value.copy(sentRequests = updated, message = "Buddy request sent to ${buddy.name}!")
        }
    }

    fun clearMessage() { _state.value = _state.value.copy(message = null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuddyMatchingScreen(
    onBack: () -> Unit,
    vm: BuddyViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            kotlinx.coroutines.delay(1500)
            vm.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, null, tint = LpuOrange)
                        Spacer(Modifier.width(8.dp))
                        Text("Buddy Match", fontWeight = FontWeight.ExtraBold, color = PrimaryNavy)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryNavy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp)
        ) {
            SearchField(
                query = state.query,
                onQueryChange = vm::setQuery,
                placeholder = "Search by country, course, or name..."
            )

            Spacer(Modifier.height(16.dp))

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LpuOrange)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(state.filtered, key = { it.uid }) { buddy ->
                        val isSent = state.sentRequests.contains(buddy.uid)
                        BuddyCard(buddy, isSent) { vm.sendRequest(buddy) }
                    }
                }
            }
        }
    }
}

@Composable
fun BuddyCard(buddy: BuddyProfile, isSent: Boolean, onConnect: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale")

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp, pressedElevation = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onConnect)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Subtle gradient top tint
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Brush.verticalGradient(listOf(GlobeBlue.copy(alpha = 0.05f), Color.Transparent)))
            )

            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .shadow(12.dp, CircleShape, spotColor = GlobeBlue)
                            .clip(CircleShape)
                    ) {
                        StudentAvatar(avatarUrl = buddy.avatarUrl, name = buddy.name, size = 56.dp, showOnlineStatus = true)
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(Modifier.weight(1f)) {
                        Text(buddy.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PrimaryNavy)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Public, null, tint = GlobeBlue, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(buddy.country, fontSize = 13.sp, color = GlobeBlue, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.School, null, tint = LpuOrange, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(buddy.course, fontSize = 13.sp, color = LpuOrange, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (buddy.bio.isNotBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        buddy.bio,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.65f),
                        lineHeight = 20.sp
                    )
                }

                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    buddy.interests.take(3).forEach { interest ->
                        Surface(
                            color = PrimaryNavy.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, PrimaryNavy.copy(alpha = 0.1f))
                        ) {
                            Text(
                                "#$interest",
                                fontSize = 12.sp,
                                color = PrimaryNavy,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Animated Connect Button
                val btnInteraction = remember { MutableInteractionSource() }
                val btnPressed by btnInteraction.collectIsPressedAsState()
                val btnScale by animateFloatAsState(if (btnPressed) 0.95f else 1f, label = "btnScale")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .scale(btnScale)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSent) Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                            else Brush.horizontalGradient(listOf(LpuOrange, GlobeBlue))
                        )
                        .clickable(
                            interactionSource = btnInteraction,
                            indication = null,
                            enabled = !isSent,
                            onClick = onConnect
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isSent) Icons.Default.CheckCircle else Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isSent) "Request Sent" else "Connect as Buddy",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}