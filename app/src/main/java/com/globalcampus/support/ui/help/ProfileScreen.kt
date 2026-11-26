package com.globalcampus.support.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.globalcampus.support.data.model.User
import com.globalcampus.support.data.repository.AuthRepository
import com.globalcampus.support.data.repository.UserRepository
import com.globalcampus.support.ui.theme.PrimaryNavy
import com.globalcampus.support.util.ImageUploader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Brand Colors
val LpuOrange = Color(0xFFF37021)
val GlobeBlue = Color(0xFF1E88E5)

// Preset theme identifiers (used only as keys — the visual is drawn with icons)
val PRESET_AVATARS = listOf(
    "avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_5"
)

// ---------- State ----------
data class ProfileState(
    val loading: Boolean = true,
    val user: User? = null,
    val saving: Boolean = false,
    val message: String? = null
)

// ---------- ViewModel ----------
class ProfileViewModel : ViewModel() {
    private val repo = UserRepository()
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val res = repo.getMyProfile()
            _state.value = _state.value.copy(loading = false, user = res.getOrNull())
        }
    }

    fun save(name: String, country: String, course: String, avatarUrl: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true, message = null)
            val res = repo.updateProfile(name, country, course, avatarUrl)
            _state.value = _state.value.copy(
                saving = false,
                message = if (res.isSuccess) "Profile updated successfully!"
                else "Failed: ${res.exceptionOrNull()?.message}"
            )
            load()
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}

// ---------- Screen ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    vm: ProfileViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val auth = remember { AuthRepository() }

    var name by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("avatar_1") }
    var customImageInput by remember { mutableStateOf("") }

    LaunchedEffect(state.user) {
        state.user?.let {
            name = it.name
            country = it.country
            course = it.course
            if (it.avatarUrl.isNotBlank()) {
                avatarUrl = it.avatarUrl
                if (it.avatarUrl.startsWith("http")) customImageInput = it.avatarUrl
            }
        }
    }

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
                    Text(
                        "My Profile",
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryNavy
                    )
                },
                actions = {
                    IconButton(onClick = { auth.signOut(); onLogout() }) {
                        Icon(Icons.Default.Logout, "Logout", tint = LpuOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (state.loading) {
            Box(
                Modifier.fillMaxSize().padding(pad),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = LpuOrange) }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GlobeBlue.copy(alpha = 0.05f),
                            LpuOrange.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gradient Profile Header
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(LpuOrange, GlobeBlue)))
                    .border(4.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl.startsWith("http")) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = name.ifBlank { "International Student" },
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryNavy
            )
            Text(
                text = auth.currentUser?.email ?: "No email",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(6.dp))

            Surface(
                color = GlobeBlue.copy(0.12f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Regd No: ${state.user?.regdNo?.ifBlank { "12204859" } ?: "12204859"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GlobeBlue,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Avatar / Theme Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Profile Theme",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryNavy
                    )
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(PRESET_AVATARS) { avatarId ->
                            val isSelected = avatarUrl == avatarId
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected)
                                            Brush.linearGradient(listOf(LpuOrange, GlobeBlue))
                                        else
                                            Brush.linearGradient(listOf(Color.LightGray, Color.Gray))
                                    )
                                    .clickable { avatarUrl = avatarId },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    var isUploading by remember { mutableStateOf(false) }
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val scope = rememberCoroutineScope()
                    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                    ) { uri ->
                        if (uri != null) {
                            scope.launch {
                                isUploading = true
                                val uploadedUrl = ImageUploader.uploadImage(context, uri)
                                isUploading = false
                                if (!uploadedUrl.isNullOrBlank()) {
                                    avatarUrl = uploadedUrl
                                    customImageInput = uploadedUrl
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { imagePicker.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = GlobeBlue.copy(0.12f), contentColor = GlobeBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = GlobeBlue)
                        } else {
                            Icon(Icons.Default.Edit, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Upload Photo via Cloudinary", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LpuOrange,
                    focusedLabelColor = LpuOrange,
                    cursorColor = LpuOrange
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.saving
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text("Home Country") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LpuOrange,
                    focusedLabelColor = LpuOrange,
                    cursorColor = LpuOrange
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.saving
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = course,
                onValueChange = { course = it },
                label = { Text("Course") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LpuOrange,
                    focusedLabelColor = LpuOrange,
                    cursorColor = LpuOrange
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.saving
            )

            Spacer(Modifier.height(20.dp))

            // Account Security & Management Options
            val authVm: com.globalcampus.support.ui.auth.AuthViewModel = viewModel()
            var showChangePassDialog by remember { mutableStateOf(false) }
            var showDeleteDialog by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Account Settings & Security", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryNavy)
                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showChangePassDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Lock, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Change Password")
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Account", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (showChangePassDialog) {
                var newPass by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showChangePassDialog = false },
                    title = { Text("Change Password", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newPass,
                                onValueChange = { newPass = it },
                                label = { Text("New Password (min 6 chars)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                authVm.changePassword(newPass)
                                showChangePassDialog = false
                            },
                            enabled = newPass.length >= 6
                        ) {
                            Text("Update Password")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showChangePassDialog = false }) { Text("Cancel") }
                    }
                )
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Account?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                    text = { Text("Are you sure you want to permanently delete your GlobalCampus account? All saved data will be lost.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                authVm.deleteAccount(onDeleted = {
                                    showDeleteDialog = false
                                    onLogout()
                                })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Permanently Delete", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                    }
                )
            }

            Spacer(Modifier.height(28.dp))

            // Gradient Save Button
            Button(
                onClick = { vm.save(name.trim(), country.trim(), course.trim(), avatarUrl.trim()) },
                enabled = !state.saving && name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(LpuOrange, GlobeBlue)),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.saving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Save Profile Changes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}