package com.globalcampus.support.ui.lostfound

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.globalcampus.support.data.model.AppNotification
import com.globalcampus.support.data.model.LostFoundItem
import com.globalcampus.support.data.repository.AuthRepository
import com.globalcampus.support.data.repository.LostFoundRepository
import com.globalcampus.support.data.repository.NotificationRepository
import com.globalcampus.support.ui.components.CategoryChip
import com.globalcampus.support.ui.theme.PrimaryNavy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

val LpuOrange = Color(0xFFF37021)
val GlobeBlue = Color(0xFF1E88E5)

data class LostFoundState(
    val loading: Boolean = true,
    val items: List<LostFoundItem> = emptyList(),
    val filter: String = "All",
    val adding: Boolean = false
) {
    val filtered: List<LostFoundItem> get() = items.filter { filter == "All" || it.type == filter }
}

class LostFoundViewModel : ViewModel() {
    private val repo = LostFoundRepository()
    private val notifRepo = NotificationRepository()
    private val auth = AuthRepository()
    private val _state = MutableStateFlow(LostFoundState())
    val state: StateFlow<LostFoundState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val res = repo.getItems()
            _state.value = _state.value.copy(loading = false, items = res.getOrDefault(emptyList()))
        }
    }

    fun setFilter(f: String) { _state.value = _state.value.copy(filter = f) }

    fun addItem(title: String, type: String, category: String, location: String, desc: String, contact: String, imageUrl: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(adding = true)
            repo.addItem(title, type, category, location, desc, contact, imageUrl, auth.currentUser?.uid ?: "")
            notifRepo.sendNotification(
                AppNotification(
                    userId = "all",
                    title = "New $type Item Reported!",
                    message = "$title reported at $location — Check Lost & Found directory."
                )
            )
            _state.value = _state.value.copy(adding = false)
            load()
        }
    }

    fun markAsResolved(item: LostFoundItem) {
        viewModelScope.launch {
            val updatedItems = _state.value.items.map { if (it.id == item.id) it.copy(isResolved = true) else it }
            _state.value = _state.value.copy(items = updatedItems)
            repo.markResolved(item.id)
            notifRepo.sendNotification(
                AppNotification(
                    userId = "all",
                    title = "Item Recovered!",
                    message = "The ${item.type} item '${item.title}' has been recovered!"
                )
            )
            val reloaded = repo.getItems()
            if (reloaded.isSuccess) {
                _state.value = _state.value.copy(items = reloaded.getOrDefault(updatedItems))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostFoundScreen(
    onBack: () -> Unit,
    vm: LostFoundViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = LpuOrange)
                        Spacer(Modifier.width(8.dp))
                        Text("Lost & Found Directory", fontWeight = FontWeight.ExtraBold, color = PrimaryNavy)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryNavy) } },
                actions = { IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.AddCircle, "Report Item", tint = GlobeBlue) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pad).background(Brush.verticalGradient(listOf(GlobeBlue.copy(0.03f), Color.Transparent)))
        ) {
            LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All", "Lost", "Found")) { f ->
                    CategoryChip(label = f, selected = state.filter == f, onClick = { vm.setFilter(f) })
                }
            }

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = LpuOrange) }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.filtered, key = { it.id }) { item ->
                        LostFoundCard(item, onMarkFound = { vm.markAsResolved(item) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddLostFoundDialog(
            adding = state.adding,
            onDismiss = { showAddDialog = false },
            onSubmit = { title, type, cat, loc, contact, imgUrl ->
                vm.addItem(title, type, cat, loc, "", contact, imgUrl)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun LostFoundCard(item: LostFoundItem, onMarkFound: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (item.type == "Lost") Color(0xFFEF4444) else Color(0xFF10B981),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(item.type.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(item.category, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GlobeBlue)
                Spacer(Modifier.weight(1f))
                if (item.isResolved) {
                    Surface(color = Color(0xFF10B981).copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                        Text("RECOVERED ✅", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(item.title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PrimaryNavy)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = LpuOrange, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(item.location, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, null, tint = GlobeBlue, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(item.contact, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
            }

            if (item.imageUrl.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = item.imageUrl, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (!item.isResolved) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onMarkFound,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Mark as Found / Recovered", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AddLostFoundDialog(adding: Boolean, onDismiss: () -> Unit, onSubmit: (String, String, String, String, String, String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Lost") }
    var location by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isUploading = true
                val uploaded = com.globalcampus.support.util.ImageUploader.uploadImage(context, uri)
                isUploading = false
                if (!uploaded.isNullOrBlank()) imageUrl = uploaded
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Item", fontWeight = FontWeight.Bold, color = PrimaryNavy) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == "Lost", onClick = { type = "Lost" }, label = { Text("Lost") })
                    FilterChip(selected = type == "Found", onClick = { type = "Found" }, label = { Text("Found") })
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Item Name") }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Info") }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = { photoPicker.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = GlobeBlue.copy(0.12f), contentColor = GlobeBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = GlobeBlue)
                    } else {
                        Icon(Icons.Default.CloudUpload, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (imageUrl.isNotBlank()) "Photo Uploaded ✅" else "Upload Photo (Cloudinary)")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(title, type, "Personal", location, contact, imageUrl)
                },
                enabled = title.isNotBlank() && location.isNotBlank() && !adding && !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = LpuOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (adding) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Submit Report", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) } },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
