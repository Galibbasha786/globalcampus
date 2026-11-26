package com.globalcampus.support.ui.community

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.globalcampus.support.data.model.Comment
import com.globalcampus.support.data.model.Post
import com.globalcampus.support.data.repository.AuthRepository
import com.globalcampus.support.data.repository.PostRepository
import com.globalcampus.support.ui.components.StudentAvatar
import com.globalcampus.support.ui.theme.PrimaryNavy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Brand Colors
val LpuOrange2 = Color(0xFFF37021)
val GlobeBlue2 = Color(0xFF1E88E5)

data class PostDetailState(
    val loading: Boolean = true,
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val posting: Boolean = false
)

class PostDetailViewModel : ViewModel() {
    private val repo = PostRepository()
    private val auth = AuthRepository()
    private val _state = MutableStateFlow(PostDetailState())
    val state: StateFlow<PostDetailState> = _state

    val myUid: String get() = auth.currentUser?.uid ?: ""
    private var postId: String = ""

    fun load(id: String) {
        postId = id
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val p = repo.getPost(id).getOrNull()
            val c = repo.getComments(id).getOrDefault(emptyList())
            _state.value = PostDetailState(loading = false, post = p, comments = c)
        }
    }

    fun addComment(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(posting = true)
            repo.addComment(postId, text.trim())
            load(postId)
        }
    }

    fun toggleLike() {
        val p = _state.value.post ?: return
        viewModelScope.launch {
            repo.toggleLike(p.id, p.likedBy.contains(myUid))
            load(postId)
        }
    }

    fun toggleResolved() {
        val p = _state.value.post ?: return
        viewModelScope.launch {
            repo.markResolved(p.id, !p.resolved)
            load(postId)
        }
    }

    fun report(reason: String) {
        viewModelScope.launch { repo.reportPost(postId, reason) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(postId: String, onBack: () -> Unit, vm: PostDetailViewModel = viewModel()) {
    LaunchedEffect(postId) { vm.load(postId) }
    val state by vm.state.collectAsStateWithLifecycle()
    var commentText by remember { mutableStateOf("") }
    var showReport by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discussion", fontWeight = FontWeight.ExtraBold, color = PrimaryNavy) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryNavy) }
                },
                actions = {
                    IconButton(onClick = { showReport = true }) {
                        Icon(Icons.Default.Flag, "Report", tint = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText, onValueChange = { commentText = it },
                        placeholder = { Text("Write a comment…") },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange2)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        enabled = commentText.isNotBlank() && !state.posting,
                        onClick = { vm.addComment(commentText); commentText = "" },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (commentText.isNotBlank()) Brush.horizontalGradient(listOf(LpuOrange2, GlobeBlue2)) else Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LpuOrange2)
            }
            return@Scaffold
        }
        val p = state.post ?: return@Scaffold

        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StudentAvatar(avatarUrl = "", name = p.authorName, size = 44.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.authorName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryNavy)
                                Text(p.category.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LpuOrange2)
                            }
                            if (p.type == "help") {
                                Surface(
                                    color = if (p.resolved) Color(0xFF10B981) else LpuOrange2,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        if (p.resolved) "RESOLVED" else "OPEN",
                                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(p.title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryNavy)
                        Spacer(Modifier.height(8.dp))
                        Text(p.body, fontSize = 15.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))

                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val liked = p.likedBy.contains(vm.myUid)
                            IconButton(onClick = { vm.toggleLike() }) {
                                Icon(
                                    if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    "Like", tint = if (liked) LpuOrange2 else MaterialTheme.colorScheme.onSurface.copy(0.5f)
                                )
                            }
                            Text("${p.likes}", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            Spacer(Modifier.weight(1f))

                            if (p.type == "help" && p.authorId == vm.myUid) {
                                OutlinedButton(
                                    onClick = { vm.toggleResolved() },
                                    border = BorderStroke(1.dp, LpuOrange2),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LpuOrange2)
                                ) {
                                    Text(if (p.resolved) "Reopen" else "Mark Resolved")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Comments (${state.comments.size})",
                    fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PrimaryNavy,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(state.comments, key = { it.id }) { c ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        StudentAvatar(avatarUrl = "", name = c.authorName, size = 32.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(c.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GlobeBlue2)
                            Spacer(Modifier.height(4.dp))
                            Text(c.text, fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.85f))
                        }
                    }
                }
            }
        }
    }

    if (showReport) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReport = false },
            title = { Text("Report Post", fontWeight = FontWeight.Bold, color = PrimaryNavy) },
            text = {
                OutlinedTextField(
                    value = reason, onValueChange = { reason = it },
                    label = { Text("Reason for report") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange2),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    enabled = reason.isNotBlank(),
                    onClick = { vm.report(reason); showReport = false },
                    colors = ButtonDefaults.buttonColors(containerColor = LpuOrange2),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Submit", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showReport = false }) { Text("Cancel", color = Color.Gray) } },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}