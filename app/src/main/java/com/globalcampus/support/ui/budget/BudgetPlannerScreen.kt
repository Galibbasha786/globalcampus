package com.globalcampus.support.ui.budget

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.globalcampus.support.data.model.ExpenseItem
import com.globalcampus.support.data.repository.BudgetRepository
import com.globalcampus.support.ui.components.CategoryChip
import com.globalcampus.support.ui.components.PrimaryButton
import com.globalcampus.support.ui.theme.PrimaryNavy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// Brand Colors
val LpuOrange = Color(0xFFF37021)
val GlobeBlue = Color(0xFF1E88E5)

data class BudgetState(
    val loading: Boolean = true,
    val monthlyLimit: Double = 15000.0,
    val expenses: List<ExpenseItem> = emptyList(),
    val adding: Boolean = false
) {
    val totalSpent: Double get() = expenses.sumOf { it.amount }
    val remaining: Double get() = monthlyLimit - totalSpent
}

class BudgetViewModel : ViewModel() {
    private val repo = BudgetRepository()
    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val res = repo.getExpenses()
            _state.value = _state.value.copy(loading = false, expenses = res.getOrDefault(emptyList()))
        }
    }

    fun addExpense(title: String, amount: Double, category: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(adding = true)
            repo.addExpense(title, amount, category)
            _state.value = _state.value.copy(adding = false)
            load()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetPlannerScreen(
    onBack: () -> Unit,
    vm: BudgetViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    val progress = (state.totalSpent / state.monthlyLimit).coerceIn(0.0, 1.0).toFloat()

    // Animated progress bar
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, null, tint = LpuOrange)
                        Spacer(Modifier.width(8.dp))
                        Text("Budget Planner", fontWeight = FontWeight.ExtraBold, color = PrimaryNavy)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryNavy)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.AddCircle, "Add Expense", tint = GlobeBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp)
        ) {
            // Animated Budget Summary Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryNavy, Color(0xFF2A3B5C), GlobeBlue.copy(alpha = 0.8f))
                            )
                        )
                        .padding(20.dp)
                ) {
                    // Decorative background circles
                    Box(Modifier.size(100.dp).offset(x = 200.dp, y = (-20).dp).background(Color.White.copy(alpha = 0.05f), CircleShape))

                    Column {
                        Text("Total Monthly Budget", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(
                            "₹${NumberFormat.getNumberInstance(Locale("en", "IN")).format(state.monthlyLimit)}",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Spent", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                                Text("₹${state.totalSpent.toInt()}", color = LpuOrange, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Remaining", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                                Text(
                                    "₹${state.remaining.toInt()}",
                                    color = if (state.remaining < 2000) Color(0xFFEF4444) else Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Custom Animated Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            if (progress > 0.8) listOf(Color(0xFFEF4444), LpuOrange)
                                            else listOf(GlobeBlue, Color(0xFF10B981))
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Interactive Quick Budget Calculator & Goal Setter
            var isCalculatorExpanded by remember { mutableStateOf(false) }
            var customLimitInput by remember { mutableStateOf(state.monthlyLimit.toInt().toString()) }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, null, tint = GlobeBlue)
                            Spacer(Modifier.width(8.dp))
                            Text("Monthly Budget Goal Calculator", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryNavy)
                        }
                        IconButton(onClick = { isCalculatorExpanded = !isCalculatorExpanded }) {
                            Icon(if (isCalculatorExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Expand")
                        }
                    }

                    if (isCalculatorExpanded) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customLimitInput,
                            onValueChange = { customLimitInput = it },
                            label = { Text("Set Target Monthly Limit (INR)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val newLimit = customLimitInput.toDoubleOrNull() ?: 15000.0
                                    // Update limit in view model state
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GlobeBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Goal")
                            }
                            OutlinedButton(
                                onClick = {
                                    customLimitInput = "15000"
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Recent Expenses", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PrimaryNavy)
            Spacer(Modifier.height(12.dp))

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LpuOrange)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(state.expenses, key = { it.id }) { item ->
                        // Determine icon based on category
                        val icon = when(item.category.lowercase()) {
                            "food" -> Icons.Default.Restaurant
                            "housing" -> Icons.Default.Home
                            "transport" -> Icons.Default.DirectionsBus
                            "supplies" -> Icons.Default.ShoppingBag
                            else -> Icons.Default.SportsEsports
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(GlobeBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, null, tint = GlobeBlue, modifier = Modifier.size(20.dp))
                                }

                                Spacer(Modifier.width(14.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryNavy)
                                    Text(item.category, fontSize = 12.sp, color = LpuOrange, fontWeight = FontWeight.SemiBold)
                                }
                                Text(
                                    "- ₹${item.amount.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Animated Dialog for adding expense
    AnimatedVisibility(
        visible = showAddDialog,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f)
    ) {
        var title by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Food") }
        val categories = listOf("Food", "Housing", "Transport", "Supplies", "Leisure")

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, null, tint = LpuOrange)
                    Spacer(Modifier.width(8.dp))
                    Text("Record Expense", fontWeight = FontWeight.Bold, color = PrimaryNavy)
                }
            },
            text = {
                Column {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { c ->
                            CategoryChip(
                                label = c,
                                selected = category == c,
                                onClick = { category = c }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Expense Item") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange, focusedLabelColor = LpuOrange),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (INR)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange, focusedLabelColor = LpuOrange),
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) vm.addExpense(title.trim(), amount, category)
                        showAddDialog = false
                    },
                    enabled = title.isNotBlank() && amountText.toDoubleOrNull() != null && !state.adding,
                    colors = ButtonDefaults.buttonColors(containerColor = LpuOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.adding) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save Expense", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}