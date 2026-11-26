package com.globalcampus.support.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.globalcampus.support.ui.theme.SecondaryRoyalBlue

data class VisaChecklistItem(
    val title: String,
    val description: String,
    val isMandatory: Boolean = true,
    var checked: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisaToolsScreen(onBack: () -> Unit) {
    var checklist by remember {
        mutableStateOf(
            listOf(
                VisaChecklistItem("Passport Original", "Valid for at least 6 months", checked = true),
                VisaChecklistItem("Bonafide Certificate", "Download from UMS -> Academic Documents", checked = true),
                VisaChecklistItem("C-Form Copy", "Obtained from Hostel Warden / Landlord", checked = false),
                VisaChecklistItem("Bank Statement / Financial Proof", "Showing sufficient tuition & stay funds", checked = false),
                VisaChecklistItem("Passport Photos (4 copies)", "White background, 3.5 x 4.5 cm", checked = false),
                VisaChecklistItem("Registration at e-FRRO Portal", "Submit online at indianfrro.gov.in 30 days before expiry", checked = false),
                VisaChecklistItem("DIA Office Verification", "Get physical stamp at Block 32", checked = false)
            )
        )
    }

    val completedCount = checklist.count { it.checked }
    val progress = if (checklist.isNotEmpty()) completedCount.toFloat() / checklist.size else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🛂 AI Visa & FRRO Assistant", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp)
        ) {
            // Progress Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("FRRO Document Checklist", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$completedCount of ${checklist.size} documents ready",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = SecondaryRoyalBlue
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Mandatory Documents for DIA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(checklist) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.checked,
                                onCheckedChange = { isChecked ->
                                    checklist = checklist.map {
                                        if (it.title == item.title) it.copy(checked = isChecked) else it
                                    }
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    item.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Visit Block 32 DIA Counter 4 for FRRO Submission.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}