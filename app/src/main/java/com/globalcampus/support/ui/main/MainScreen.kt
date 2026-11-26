package com.globalcampus.support.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.globalcampus.support.data.repository.AuthRepository
import com.globalcampus.support.ui.info.InfoListScreen
import com.globalcampus.support.ui.navigation.NavGraph   // not used, just placeholder

enum class BottomTab(val label: String) {
    HOME("Home"), INFO("Info"), COMMUNITY("Community"), HELP("Help"), PROFILE("Profile")
}

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onOpenArticle: (String) -> Unit
) {
    var selected by remember { mutableStateOf(BottomTab.INFO) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selected == BottomTab.HOME, onClick = { selected = BottomTab.HOME },
                    icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = selected == BottomTab.INFO, onClick = { selected = BottomTab.INFO },
                    icon = { Icon(Icons.Default.Info, null) }, label = { Text("Info") })
                NavigationBarItem(selected = selected == BottomTab.COMMUNITY, onClick = { selected = BottomTab.COMMUNITY },
                    icon = { Icon(Icons.Default.People, null) }, label = { Text("Community") })
                NavigationBarItem(selected = selected == BottomTab.HELP, onClick = { selected = BottomTab.HELP },
                    icon = { Icon(Icons.Default.Chat, null) }, label = { Text("Help") })
                NavigationBarItem(selected = selected == BottomTab.PROFILE, onClick = { selected = BottomTab.PROFILE },
                    icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selected) {
                BottomTab.HOME -> PlaceholderTab("🏠 Home", "Day 2 will add personalized feed + AI recommendations")
                BottomTab.INFO -> InfoListScreen(onOpenArticle = onOpenArticle)
                BottomTab.COMMUNITY -> PlaceholderTab("👥 Community", "Day 2 — Feed, posts, likes, comments")
                BottomTab.HELP -> PlaceholderTab("🤝 Help", "Day 2 — Ask for help, replies, resolved")
                BottomTab.PROFILE -> ProfilePlaceholder(onLogout)
            }
        }
    }
}

@Composable
private fun PlaceholderTab(title: String, subtitle: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Text(subtitle, color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
        }
    }
}

@Composable
private fun ProfilePlaceholder(onLogout: () -> Unit) {
    val auth = AuthRepository()
    val user = auth.currentUser
    androidx.compose.foundation.layout.Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text("👤 Profile", style = MaterialTheme.typography.headlineMedium)
        Text(user?.email ?: "", color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
        Spacer()
        Button(onClick = { auth.signOut(); onLogout() }) { Text("Logout") }
    }
}

@Composable
private fun Spacer() {
    androidx.compose.foundation.layout.Spacer(Modifier.padding(12.dp))
}