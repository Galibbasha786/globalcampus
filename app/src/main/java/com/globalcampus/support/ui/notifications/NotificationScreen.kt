package com.globalcampus.support.ui.notifications

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.globalcampus.support.data.model.AppNotification
import com.globalcampus.support.ui.theme.PrimaryNavy
import java.text.SimpleDateFormat
import java.util.*

val LpuOrange = Color(0xFFF37021)
val GlobeBlue = Color(0xFF1E88E5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    vm: NotificationsViewModel = viewModel()
) {
    val notifications by vm.notifications.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    var prevCount by remember { mutableIntStateOf(notifications.size) }

    LaunchedEffect(notifications.size) {
        if (notifications.size > prevCount) {
            try {
                val notificationUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = android.media.RingtoneManager.getRingtone(context, notificationUri)
                ringtone?.play()
            } catch (e: Exception) { e.printStackTrace() }
        }
        prevCount = notifications.size
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifications",
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryNavy
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryNavy)
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { vm.deleteAllNotifications() }) {
                            Icon(Icons.Default.DeleteSweep, "Delete All", tint = Color.Red)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(pad),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = GlobeBlue.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No notifications yet",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "You'll see updates here when students connect or report items.",
                        color = Color.Gray.copy(0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .background(
                        Brush.verticalGradient(
                            listOf(GlobeBlue.copy(0.03f), Color.Transparent)
                        )
                    ),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                vm.deleteNotification(notif.id)
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Red.copy(0.2f))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                            }
                        }
                    ) {
                        NotificationCard(
                            notif = notif,
                            onAcceptBuddy = { vm.acceptBuddyRequest(notif) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notif: AppNotification, onAcceptBuddy: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val dateString = formatter.format(Date(notif.timestamp))
    val isBuddyRequest = notif.title.contains("Buddy", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LpuOrange.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, null, tint = LpuOrange)
                    if (!notif.isRead) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(10.dp)
                                .scale(scale)
                                .background(Color.Red, CircleShape)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        notif.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryNavy
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        notif.message,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(dateString, fontSize = 11.sp, color = Color.Gray)
                }
            }

            if (isBuddyRequest) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onAcceptBuddy,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Accept Buddy Request", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
