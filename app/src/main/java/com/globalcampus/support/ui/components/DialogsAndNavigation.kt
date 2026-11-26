package com.globalcampus.support.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.globalcampus.support.ui.navigation.Routes
import com.globalcampus.support.ui.theme.PrimaryNavy
import com.globalcampus.support.ui.theme.SecondaryRoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalCampusTopBar(
    title: String,
    avatarUrl: String = "",
    unreadCount: Int = 0,
    onMenuClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    onAiClick: (() -> Unit)? = null,
    onNotificationClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onMenuClick != null) {
                    StudentAvatar(
                        avatarUrl = avatarUrl,
                        name = "Student",
                        size = 32.dp,
                        showOnlineStatus = true
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        navigationIcon = {
            when {
                onBackClick != null -> {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                onMenuClick != null -> {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = PrimaryNavy)
                    }
                }
            }
        },
        actions = {
            if (onNotificationClick != null) {
                IconButton(onClick = onNotificationClick) {
                    Box {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = PrimaryNavy)
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            if (onAiClick != null) {
                IconButton(onClick = onAiClick) {
                    Icon(Icons.Default.SmartToy, contentDescription = "AI Advisor", tint = SecondaryRoyalBlue)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun GlobalCampusBottomBar(
    navController: NavHostController,
    currentRoute: String?
) {
    val tabs = listOf(
        Triple(Routes.HOME, "Home", Icons.Default.Home),
        Triple(Routes.COMMUNITY, "Community", Icons.Default.Groups),
        Triple(Routes.HELP, "Help", Icons.Default.Support),
        Triple(Routes.CHAT, "AI Bot", Icons.Default.SmartToy),
        Triple(Routes.PROFILE, "Profile", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        tabs.forEach { (route, label, icon) ->
            val selected = currentRoute == route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = {
                    Text(
                        text = label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SecondaryRoyalBlue,
                    selectedTextColor = SecondaryRoyalBlue,
                    indicatorColor = SecondaryRoyalBlue.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) MaterialTheme.colorScheme.error else SecondaryRoyalBlue
                )
            ) {
                Text(confirmText, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
