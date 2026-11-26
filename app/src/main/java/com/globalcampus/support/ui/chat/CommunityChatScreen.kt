package com.globalcampus.support.ui.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.globalcampus.support.data.model.ChatGroup
import com.globalcampus.support.data.model.ChatMessageItem
import com.globalcampus.support.data.model.UserStory
import com.globalcampus.support.data.repository.AuthRepository
import com.globalcampus.support.data.repository.ChatRepository
import com.globalcampus.support.ui.components.CategoryChip
import com.globalcampus.support.ui.components.StudentAvatar
import com.globalcampus.support.ui.theme.PrimaryNavy
import com.globalcampus.support.util.ImageUploader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

val LpuOrange = Color(0xFFF37021)
val GlobeBlue = Color(0xFF1E88E5)
val WhatsAppGreen = Color(0xFF075E54)

data class CommunityChatState(
    val selectedGroup: ChatGroup? = null,
    val groups: List<ChatGroup> = emptyList(),
    val messages: List<ChatMessageItem> = emptyList(),
    val stories: List<UserStory> = emptyList(),
    val hasActiveCall: Boolean = false,
    val realUsers: List<com.globalcampus.support.data.model.User> = emptyList(),
    val creatingGroup: Boolean = false
)

class CommunityChatViewModel : ViewModel() {
    private val repo = ChatRepository()
    private val auth = AuthRepository()
    private val _state = MutableStateFlow(CommunityChatState())
    val state: StateFlow<CommunityChatState> = _state

    val myUid get() = auth.currentUser?.uid ?: ""

    init {
        loadGroups()
        loadRealUsers()
        listenToStories()
    }

    private fun loadRealUsers() {
        viewModelScope.launch {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            try {
                val snap = db.collection("users").get().await()
                val users = snap.documents.mapNotNull { doc -> doc.toObject(com.globalcampus.support.data.model.User::class.java) }
                _state.value = _state.value.copy(realUsers = users)
            } catch (_: Exception) {}
        }
    }

    private fun listenToStories() {
        viewModelScope.launch {
            repo.getRealtimeStories().collect { storyList ->
                _state.value = _state.value.copy(stories = storyList)
            }
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            val res = repo.getGroups()
            val groups = res.getOrDefault(emptyList())
            val default = groups.firstOrNull { it.id == "global" } ?: groups.firstOrNull()
            _state.value = _state.value.copy(groups = groups, selectedGroup = default)
            default?.let {
                listenToMessages(it.id)
                listenToCall(it.id)
            }
        }
    }

    fun selectGroup(group: ChatGroup) {
        _state.value = _state.value.copy(selectedGroup = group)
        listenToMessages(group.id)
        listenToCall(group.id)
    }

    private fun listenToMessages(chatId: String) {
        viewModelScope.launch {
            repo.getRealtimeMessages(chatId).collect { msgs ->
                _state.value = _state.value.copy(messages = msgs)
            }
        }
    }

    private fun listenToCall(groupId: String) {
        viewModelScope.launch {
            repo.getRealtimeActiveCall(groupId).collect { active ->
                _state.value = _state.value.copy(hasActiveCall = active)
            }
        }
    }

    fun startGroupCall() {
        val grp = _state.value.selectedGroup ?: return
        viewModelScope.launch { repo.startGroupCall(grp.id, grp.name) }
    }

    fun postStory(imageUrl: String) {
        viewModelScope.launch { repo.postStory(imageUrl) }
    }

    fun sendMessage(text: String) {
        val group = _state.value.selectedGroup ?: return
        if (text.isBlank()) return
        viewModelScope.launch { repo.sendMessage(group.id, text.trim()) }
    }

    fun createGroup(name: String, desc: String, course: String, country: String, emoji: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(creatingGroup = true)
            repo.createGroup(name, desc, course, country, emoji)
            _state.value = _state.value.copy(creatingGroup = false)
            loadGroups()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityChatScreen(
    onBack: () -> Unit,
    onStartVideoCall: () -> Unit = {},
    vm: CommunityChatViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var isUploadingMedia by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isUploadingMedia = true
                val uploadedUrl = ImageUploader.uploadImage(context, uri)
                isUploadingMedia = false
                if (!uploadedUrl.isNullOrBlank()) {
                    vm.sendMessage("📷 Attached Media: $uploadedUrl")
                }
            }
        }
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Forum, null, tint = WhatsAppGreen)
                        Spacer(Modifier.width(8.dp))
                        Text(state.selectedGroup?.name ?: "Global Chat", fontWeight = FontWeight.ExtraBold, color = PrimaryNavy)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryNavy) }
                },
                actions = {
                    IconButton(onClick = {
                        vm.startGroupCall()
                        onStartVideoCall()
                    }) {
                        Icon(Icons.Default.Videocam, "Live Video Call", tint = WhatsAppGreen)
                    }
                    IconButton(onClick = { showCreateGroupDialog = true }) {
                        Icon(Icons.Default.GroupAdd, "Create Group", tint = GlobeBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { photoPicker.launch("image/*") },
                        enabled = !isUploadingMedia
                    ) {
                        if (isUploadingMedia) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.AttachFile, "Attach Media", tint = GlobeBlue)
                        }
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type a message…") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WhatsAppGreen)
                    )
                    Spacer(Modifier.width(6.dp))
                    IconButton(
                        enabled = inputText.isNotBlank(),
                        onClick = { vm.sendMessage(inputText); inputText = "" },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) WhatsAppGreen else Color.Gray)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pad)
        ) {
            // WhatsApp-Style Active Call Banner
            if (state.hasActiveCall) {
                Surface(
                    color = Color(0xFF10B981),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VideoCall, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "🟢 Live Group Video Call in Progress!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = onStartVideoCall,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Join Call 🎥", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // WhatsApp-Style Status / Stories Header Carousel
            var selectedStoryUrl by remember { mutableStateOf<String?>(null) }

            val storyPicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) {
                    scope.launch {
                        val uploadedStory = ImageUploader.uploadImage(context, uri)
                        if (!uploadedStory.isNullOrBlank()) {
                            vm.postStory(uploadedStory)
                            selectedStoryUrl = uploadedStory
                        }
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { storyPicker.launch("image/*") }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Gray, CircleShape)
                                .padding(3.dp)
                        ) {
                            StudentAvatar(avatarUrl = "", name = "My Status", size = 50.dp)
                            Icon(
                                Icons.Default.AddCircle, null, tint = WhatsAppGreen,
                                modifier = Modifier.size(18.dp).align(Alignment.BottomEnd)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("My Status", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PrimaryNavy)
                    }
                }

                items(state.stories, key = { it.id }) { story ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedStoryUrl = story.imageUrl }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(2.dp, WhatsAppGreen, CircleShape)
                                .padding(3.dp)
                        ) {
                            StudentAvatar(avatarUrl = story.authorAvatar, name = story.authorName, size = 50.dp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(story.authorName.substringBefore(" "), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PrimaryNavy)
                    }
                }
            }

            if (selectedStoryUrl != null) {
                AlertDialog(
                    onDismissRequest = { selectedStoryUrl = null },
                    title = { Text("Student Story", fontWeight = FontWeight.Bold, color = PrimaryNavy) },
                    text = {
                        Box(modifier = Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(16.dp))) {
                            coil.compose.AsyncImage(
                                model = selectedStoryUrl,
                                contentDescription = "Story",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedStoryUrl = null }) { Text("Close", fontWeight = FontWeight.Bold) }
                    },
                    shape = RoundedCornerShape(24.dp)
                )
            }

            HorizontalDivider(color = Color.LightGray.copy(0.3f))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.groups, key = { it.id }) { grp ->
                    CategoryChip(
                        label = grp.name,
                        selected = state.selectedGroup?.id == grp.id,
                        onClick = { vm.selectGroup(grp) }
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.messages, key = { it.id }) { msg ->
                    val isMe = msg.senderId == vm.myUid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        if (!isMe) {
                            StudentAvatar(avatarUrl = msg.senderAvatar, name = msg.senderName, size = 32.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp, topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp, bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            color = if (isMe) Color(0xFFE7FFDB) else MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (!isMe) {
                                    Text(msg.senderName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GlobeBlue)
                                    Spacer(Modifier.height(2.dp))
                                }

                                // Check if message contains an image URL (Cloudinary or other image links)
                                val imageUrlPattern = Regex("https?://[^\\s]+\\.(jpg|jpeg|png|gif|webp|bmp)(\\?[^\\s]*)?", RegexOption.IGNORE_CASE)
                                val cloudinaryPattern = Regex("https?://res\\.cloudinary\\.com/[^\\s]+", RegexOption.IGNORE_CASE)
                                val imageUrl = imageUrlPattern.find(msg.text)?.value ?: cloudinaryPattern.find(msg.text)?.value

                                if (imageUrl != null) {
                                    // Show the actual image
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Shared Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                    // Show caption text if there's text beyond the URL
                                    val captionText = msg.text
                                        .replace(imageUrl, "")
                                        .replace("📷 Attached Media:", "")
                                        .trim()
                                    if (captionText.isNotBlank()) {
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            captionText, fontSize = 14.sp, lineHeight = 20.sp,
                                            color = Color.Black
                                        )
                                    }
                                } else {
                                    Text(
                                        msg.text, fontSize = 14.sp, lineHeight = 20.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateGroupDialog) {
        var groupName by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var course by remember { mutableStateOf("") }
        var country by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            title = { Text("Create Group", fontWeight = FontWeight.Bold, color = PrimaryNavy) },
            text = {
                Column {
                    OutlinedTextField(
                        value = groupName, onValueChange = { groupName = it },
                        label = { Text("Group Name") }, singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = course, onValueChange = { course = it },
                        label = { Text("Course (Optional)") }, singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = country, onValueChange = { country = it },
                        label = { Text("Country (Optional)") }, singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.createGroup(groupName.trim(), desc.trim(), course.trim(), country.trim(), "")
                        showCreateGroupDialog = false
                    },
                    enabled = groupName.isNotBlank() && !state.creatingGroup,
                    colors = ButtonDefaults.buttonColors(containerColor = LpuOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.creatingGroup) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showCreateGroupDialog = false }) { Text("Cancel", color = Color.Gray) } },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
