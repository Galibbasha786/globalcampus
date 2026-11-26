package com.globalcampus.support.ui.events

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.globalcampus.support.data.model.EventItem
import com.globalcampus.support.data.repository.AuthRepository
import com.globalcampus.support.data.repository.EventRepository
import com.globalcampus.support.ui.components.CategoryChip
import com.globalcampus.support.ui.theme.AccentTeal
import com.globalcampus.support.ui.theme.SecondaryRoyalBlue
import com.globalcampus.support.util.ImageUploader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EventsState(
    val loading: Boolean = true,
    val events: List<EventItem> = emptyList(),
    val category: String = "All"
) {
    val categories = listOf("All", "Cultural", "Social", "Sports", "Academic")
    val filtered: List<EventItem> get() = events.filter { category == "All" || it.category == category }
}

class EventsViewModel : ViewModel() {
    private val repo = EventRepository()
    private val auth = AuthRepository()
    private val _state = MutableStateFlow(EventsState())
    val state: StateFlow<EventsState> = _state

    val myUid get() = auth.currentUser?.uid ?: ""

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val res = repo.getEvents()
            _state.value = _state.value.copy(loading = false, events = res.getOrDefault(emptyList()))
        }
    }

    fun setCategory(c: String) { _state.value = _state.value.copy(category = c) }

    fun addEvent(title: String, desc: String, date: String, time: String, location: String, category: String, imageUrl: String) {
        viewModelScope.launch {
            val event = EventItem(
                title = title,
                description = desc,
                date = date,
                time = time,
                location = location,
                category = category,
                imageUrl = imageUrl,
                organizer = auth.currentUser?.email?.substringBefore("@") ?: "LPU DIA"
            )
            repo.addEvent(event)
            load()
        }
    }

    fun toggleRsvp(event: EventItem) {
        val rsvped = event.rsvps.contains(myUid)
        viewModelScope.launch {
            val updatedList = if (rsvped) event.rsvps - myUid else event.rsvps + myUid
            val updatedEvents = _state.value.events.map { if (it.id == event.id) it.copy(rsvps = updatedList) else it }
            _state.value = _state.value.copy(events = updatedEvents)
            repo.toggleRsvp(event.id, rsvped)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onBack: () -> Unit,
    vm: EventsViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LPU Cultural & Campus Events", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.AddCircle, "Add Event", tint = SecondaryRoyalBlue)
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.categories) { cat ->
                    CategoryChip(
                        label = cat,
                        selected = state.category == cat,
                        onClick = { vm.setCategory(cat) }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SecondaryRoyalBlue)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.filtered, key = { it.id }) { event ->
                        val rsvped = event.rsvps.contains(vm.myUid)

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = SecondaryRoyalBlue.copy(0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(event.category.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondaryRoyalBlue, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Text("Organizer: ${event.organizer}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                }

                                Spacer(Modifier.height(8.dp))
                                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(event.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f), lineHeight = 18.sp)

                                if (event.imageUrl.isNotBlank()) {
                                    Spacer(Modifier.height(10.dp))
                                    AsyncImage(
                                        model = event.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Event, null, tint = SecondaryRoyalBlue, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("${event.date} • ${event.time.ifBlank { "10:00 AM" }}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.width(16.dp))
                                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(event.location, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Spacer(Modifier.height(14.dp))
                                Button(
                                    onClick = { vm.toggleRsvp(event) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (rsvped) AccentTeal else SecondaryRoyalBlue
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (rsvped) {
                                        Icon(Icons.Default.CheckCircle, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("RSVP Confirmed (${event.rsvps.size} attending)")
                                    } else {
                                        Text("RSVP to Event (${event.rsvps.size} attending)")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateEventDialog(
            onDismiss = { showCreateDialog = false },
            onSubmit = { title, desc, date, time, loc, cat, imgUrl ->
                vm.addEvent(title, desc, date, time, loc, cat, imgUrl)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CreateEventDialog(onDismiss: () -> Unit, onSubmit: (String, String, String, String, String, String, String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("25 Nov 2025") }
    var time by remember { mutableStateOf("10:00 AM") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Cultural") }
    var mediaUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    val mediaPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                isUploading = true
                val uploaded = ImageUploader.uploadImage(context, uri)
                isUploading = false
                if (!uploaded.isNullOrBlank()) mediaUrl = uploaded
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Campus Event", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location (e.g. Unipolis)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = { mediaPicker.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryRoyalBlue.copy(0.12f), contentColor = SecondaryRoyalBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isUploading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    else {
                        Icon(Icons.Default.CloudUpload, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (mediaUrl.isNotBlank()) "Media Attached ✅" else "Upload Photo/Video (Cloudinary)")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(title, desc, date, time, location, category, mediaUrl) },
                enabled = title.isNotBlank() && location.isNotBlank() && !isUploading
            ) {
                Text("Publish Event")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
