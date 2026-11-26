package com.globalcampus.support.ui.academic

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.globalcampus.support.data.model.AcademicTask
import com.globalcampus.support.data.repository.AcademicRepository
import com.globalcampus.support.ui.components.PrimaryButton
import com.globalcampus.support.ui.theme.PrimaryNavy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Brand Colors
val LpuOrange = Color(0xFFF37021)
val GlobeBlue = Color(0xFF1E88E5)

data class AcademicState(
    val loading: Boolean = true,
    val tasks: List<AcademicTask> = emptyList(),
    val adding: Boolean = false
)

class AcademicViewModel : ViewModel() {
    private val repo = AcademicRepository()
    private val _state = MutableStateFlow(AcademicState())
    val state: StateFlow<AcademicState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val res = repo.getTasks()
            _state.value = _state.value.copy(loading = false, tasks = res.getOrDefault(emptyList()))
        }
    }

    fun toggleTask(task: AcademicTask) {
        viewModelScope.launch {
            val updated = _state.value.tasks.map { if (it.id == task.id) it.copy(completed = !it.completed) else it }
            _state.value = _state.value.copy(tasks = updated)
            repo.toggleTask(task.id, !task.completed)
        }
    }

    fun addTask(subject: String, title: String, deadline: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(adding = true)
            repo.addTask(subject, title, deadline)
            _state.value = _state.value.copy(adding = false)
            load()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicToolkitScreen(
    onBack: () -> Unit,
    vm: AcademicViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, null, tint = LpuOrange)
                        Spacer(Modifier.width(8.dp))
                        Text("Academic Toolkit", fontWeight = FontWeight.ExtraBold, color = PrimaryNavy)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryNavy)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.AddCircle, "Add Task", tint = GlobeBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(
                    Brush.verticalGradient(listOf(GlobeBlue.copy(alpha = 0.03f), Color.Transparent))
                )
        ) {
            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LpuOrange)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(state.tasks, key = { it.id }) { task ->
                        AcademicTaskCard(task, onToggle = { vm.toggleTask(task) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var subject by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("") }
        var deadline by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assignment, null, tint = LpuOrange)
                    Spacer(Modifier.width(8.dp))
                    Text("New Deadline", fontWeight = FontWeight.Bold, color = PrimaryNavy)
                }
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject (e.g. CSE320)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Due Date (e.g. 25 Nov)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.addTask(subject.trim(), title.trim(), deadline.trim())
                        showAddDialog = false
                    },
                    enabled = subject.isNotBlank() && title.isNotBlank() && !state.adding,
                    colors = ButtonDefaults.buttonColors(containerColor = LpuOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.adding) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Save Task", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = Color.Gray) } },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun AcademicTaskCard(task: AcademicTask, onToggle: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale")

    // Checkbox animation
    val checkScale by animateFloatAsState(if (task.completed) 1f else 0.8f, label = "checkScale")

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onToggle)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom Animated Checkbox
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .scale(checkScale)
                    .clip(CircleShape)
                    .background(if (task.completed) Color(0xFF10B981) else GlobeBlue.copy(alpha = 0.1f))
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (task.completed) Icons.Default.Check else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (task.completed) Color.White else GlobeBlue,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Surface(
                    color = LpuOrange.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        task.subject,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LpuOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (task.completed) MaterialTheme.colorScheme.onSurface.copy(0.4f) else PrimaryNavy,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, null, tint = if (task.completed) Color.Gray else Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Due: ${task.deadline}",
                        fontSize = 12.sp,
                        color = if (task.completed) Color.Gray else Color(0xFFEF4444),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}