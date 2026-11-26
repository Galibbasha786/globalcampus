package com.globalcampus.support.ui.doc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.globalcampus.support.data.repository.GeminiRepository
import com.globalcampus.support.ui.components.PrimaryButton
import com.globalcampus.support.ui.theme.AccentTeal
import com.globalcampus.support.ui.theme.PrimaryNavy
import com.globalcampus.support.ui.theme.SecondaryRoyalBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocExplainerScreen(onBack: () -> Unit) {
    var docText by remember { mutableStateOf("") }
    var summaryResult by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val geminiRepo = remember { GeminiRepository() }

    fun analyzeDocument() {
        if (docText.isBlank()) return
        isAnalyzing = true
        summaryResult = ""
        scope.launch {
            val prompt = "Explain this official document or circular for an international student at Lovely Professional University (LPU). Extract key deadlines, mandatory steps, and a plain-English summary:\n\n$docText"
            val aiResponse = geminiRepo.generateResponse(prompt)
            summaryResult = aiResponse
            isAnalyzing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Document Explainer", fontWeight = FontWeight.Bold) },
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
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = AccentTeal)
                        Spacer(Modifier.width(8.dp))
                        Text("Gemini AI Document Breakdown", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Paste official LPU notices, fee circulars, or FRRO visa forms to get instant summaries & deadline extractions.", color = androidx.compose.ui.graphics.Color.White.copy(0.8f), fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = docText,
                onValueChange = { docText = it },
                label = { Text("Paste Circular / Document Text Here") },
                placeholder = { Text("e.g., 'Notice for all international students regarding FRRO registration and C-Form verification...'") },
                minLines = 5,
                shape = RoundedCornerShape(14.dp),
                leadingIcon = { Icon(Icons.Default.Description, null, tint = SecondaryRoyalBlue) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            PrimaryButton(
                text = "Explain Document with Gemini AI ✨",
                onClick = { analyzeDocument() },
                enabled = docText.isNotBlank() && !isAnalyzing,
                loading = isAnalyzing,
                modifier = Modifier.fillMaxWidth()
            )

            if (summaryResult.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(summaryResult, fontSize = 14.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
