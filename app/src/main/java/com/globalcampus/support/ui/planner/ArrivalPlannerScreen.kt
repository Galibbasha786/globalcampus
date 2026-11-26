package com.globalcampus.support.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.globalcampus.support.data.model.PlannerTask
import com.globalcampus.support.data.repository.PlannerRepository
import com.globalcampus.support.ui.components.CategoryChip
import com.globalcampus.support.ui.components.PrimaryButton
import com.globalcampus.support.ui.theme.AccentTeal
import com.globalcampus.support.ui.theme.PrimaryNavy
import com.globalcampus.support.ui.theme.SecondaryRoyalBlue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PlannerState(
    val loading: Boolean = true,
    val tasks: List<PlannerTask> = emptyList(),
    val stage: String = "Required Certificates", // Required Certificates | First Day | First Week | First Month
    val adding: Boolean = false
) {
    val stages = listOf("Required Certificates", "First Day", "First Week", "First Month")
    val filtered: List<PlannerTask> get() = tasks.filter { it.stage == stage }
}

class PlannerViewModel : ViewModel() {
    private val repo = PlannerRepository()
    private val _state = MutableStateFlow(PlannerState())
    val state: StateFlow<PlannerState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val res = repo.getTasks()
            _state.value = _state.value.copy(loading = false, tasks = res.getOrDefault(emptyList()))
        }
    }

    fun setStage(s: String) { _state.value = _state.value.copy(stage = s) }

    fun toggleTask(task: PlannerTask) {
        viewModelScope.launch {
            val updated = _state.value.tasks.map { if (it.id == task.id) it.copy(completed = !it.completed) else it }
            _state.value = _state.value.copy(tasks = updated)
            repo.toggleTask(task.id, !task.completed)
        }
    }

    fun addTask(title: String, description: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(adding = true)
            repo.addTask(title, description, _state.value.stage)
            _state.value = _state.value.copy(adding = false)
            load()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrivalPlannerScreen(
    onBack: () -> Unit,
    vm: PlannerViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    val completedCount = state.tasks.count { it.completed }
    val totalCount = state.tasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("✈️ Arrival & Settlement AI Planner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Add Task")
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
            // Overall Progress Header Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = AccentTeal)
                        Spacer(Modifier.width(8.dp))
                        Text("Settlement Progress", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "$completedCount of $totalCount arrival tasks completed",
                        color = Color.White.copy(0.8f),
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = AccentTeal,
                        trackColor = Color.White.copy(0.2f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stage Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.stages) { stage ->
                    CategoryChip(
                        label = stage,
                        selected = state.stage == stage,
                        onClick = { vm.setStage(stage) }
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.filtered, key = { it.id }) { task ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.completed,
                                    onCheckedChange = { vm.toggleTask(task) }
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        task.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (task.completed) MaterialTheme.colorScheme.onSurface.copy(0.5f) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        task.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(0.65f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Task to ${state.stage}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description / Location") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Add Task",
                    onClick = {
                        vm.addTask(title.trim(), desc.trim())
                        showAddDialog = false
                    },
                    enabled = title.isNotBlank() && !state.adding,
                    loading = state.adding
                )
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
