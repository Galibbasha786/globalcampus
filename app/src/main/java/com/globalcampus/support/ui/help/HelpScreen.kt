package com.globalcampus.support.ui.help

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.globalcampus.support.data.model.Post
import com.globalcampus.support.ui.components.CategoryChip
import com.globalcampus.support.ui.components.EmptyStateView
import com.globalcampus.support.ui.components.LoadingSkeleton
import com.globalcampus.support.ui.components.PrimaryButton
import com.globalcampus.support.ui.theme.PrimaryNavy

val LpuOrange = Color(0xFFF37021)
val GlobeBlue = Color(0xFF1E88E5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onOpenPost: (String) -> Unit,
    vm: HelpViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
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
                        Icon(Icons.Default.SupportAgent, null, tint = LpuOrange)
                        Spacer(Modifier.width(8.dp))
                        Text("Help Center", fontWeight = FontWeight.ExtraBold, color = PrimaryNavy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreate = true },
                containerColor = LpuOrange,
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, "Ask for Help") }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pad).background(Brush.verticalGradient(listOf(GlobeBlue.copy(0.03f), Color.Transparent)))
        ) {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All", "Urgent", "Open", "Resolved")) { f ->
                    CategoryChip(label = f, selected = state.filter == f, onClick = { vm.setFilter(f) })
                }
            }

            AnimatedContent(targetState = state.loading, label = "helpLoading") { loading ->
                when {
                    loading -> {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            repeat(4) { LoadingSkeleton() }
                        }
                    }
                    state.filtered.isEmpty() -> {
                        EmptyStateView(
                            title = "No Help Requests",
                            description = "No active student help requests in '${state.filter}'.",
                            icon = Icons.Default.HelpOutline,
                            actionButton = { PrimaryButton(text = "Ask for Help", onClick = { showCreate = true }) }
                        )
                    }
                    else -> {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.filtered, key = { it.id }) { post ->
                                HelpCard(post = post, onClick = { onOpenPost(post.id) })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateHelpDialog(
            creating = state.creating,
            onDismiss = { showCreate = false },
            onSubmit = { cat, title, body, urgent ->
                vm.createHelp(cat, title, body, urgent)
                showCreate = false
            }
        )
    }
}

@Composable
private fun HelpCard(post: Post, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (post.urgent && !post.resolved) {
                    Surface(color = Color(0xFFEF4444).copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                        Text("URGENT", Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                }
                if (post.resolved) {
                    Surface(color = Color(0xFF10B981).copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                        Text("RESOLVED", Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(post.category, fontSize = 11.sp, color = GlobeBlue, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(post.title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = PrimaryNavy)
            Spacer(Modifier.height(4.dp))
            Text(post.body, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f), maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("by ${post.authorName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.55f))
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Favorite, null, tint = LpuOrange, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("${post.likes}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Default.ChatBubble, null, tint = GlobeBlue, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("${post.commentCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
// Keep the CreateHelpDialog as is, it's already using clean inputs.
@Composable
private fun CreateHelpDialog(
    creating: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, Boolean) -> Unit
) {
    var category by remember { mutableStateOf("General") }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var urgent by remember { mutableStateOf(false) }
    val categories = listOf("General", "Visa", "Housing", "Money", "Health", "Study")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SupportAgent, null, tint = LpuOrange)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Ask for Student Support",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
            }
        },
        text = {
            Column {
                Text("Category", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryNavy)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { c ->
                        CategoryChip(
                            label = c,
                            selected = category == c,
                            onClick = { category = c }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Problem Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LpuOrange,
                        focusedLabelColor = LpuOrange,
                        cursorColor = LpuOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Describe what you need help with...") },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LpuOrange,
                        focusedLabelColor = LpuOrange,
                        cursorColor = LpuOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = urgent,
                        onCheckedChange = { urgent = it },
                        colors = CheckboxDefaults.colors(checkedColor = LpuOrange)
                    )
                    Text(
                        "Mark as URGENT request",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(category, title.trim(), body.trim(), urgent) },
                enabled = !creating && title.isNotBlank() && body.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = LpuOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (creating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Post Request", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}