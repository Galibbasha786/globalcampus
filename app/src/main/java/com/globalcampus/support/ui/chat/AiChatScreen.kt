package com.globalcampus.support.ui.chat

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.globalcampus.support.ui.components.CategoryChip
import com.globalcampus.support.ui.theme.PrimaryNavy
import com.globalcampus.support.util.TextToSpeechManager
import com.globalcampus.support.util.createSpeechToTextIntent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Brand Colors
val LpuOrange1 = Color(0xFFF37021)
val GlobeBlue1 = Color(0xFF1E88E5)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

val QUICK_PROMPTS = listOf(
    "How to apply for FRRO Visa Extension?",
    "Hostel Curfew & Guest Rules",
    "Where is Uni-Hospital on Campus?",
    "How to check Attendance & CGPA?",
    "Local Auto/Bus guide to Jalandhar"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    onOpenVisaTools: () -> Unit = {},
    onOpenPhrasebook: () -> Unit = {}
) {
    val context = LocalContext.current
    val ttsManager = remember { TextToSpeechManager(context) }
    DisposableEffect(Unit) { onDispose { ttsManager.shutdown() } }

    var selectedLang by remember { mutableStateOf("en") }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    text = "Welcome! I'm GlobalBot, your LPU International Student Assistant. Ask me in English, Hindi, or Punjabi about FRRO, hostels, exams, or campus life!",
                    isUser = false
                )
            )
        )
    }
    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Bot thinking animation
    val infiniteTransition = rememberInfiniteTransition(label = "botAnim")
    val botPulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "botPulse"
    )

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) inputText = spokenText
        }
    }

    val geminiRepo = remember { com.globalcampus.support.data.repository.GeminiRepository() }

    fun sendMessage(promptText: String) {
        if (promptText.isBlank()) return
        messages = messages + ChatMessage(text = promptText.trim(), isUser = true)
        inputText = ""
        isThinking = true
        scope.launch {
            listState.animateScrollToItem(messages.size - 1)
            val aiResponse = geminiRepo.generateResponse(promptText)
            messages = messages + ChatMessage(text = aiResponse, isUser = false)
            isThinking = false
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
            ttsManager.speak(aiResponse, selectedLang)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .scale(botPulse)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(LpuOrange1, GlobeBlue1))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Campus AI", fontWeight = FontWeight.ExtraBold, color = PrimaryNavy, fontSize = 18.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                Spacer(Modifier.width(4.dp))
                                Text("Online", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenVisaTools) { Icon(Icons.Default.FlightTakeoff, "Visa Tools", tint = PrimaryNavy) }
                    IconButton(onClick = onOpenPhrasebook) { Icon(Icons.Default.Translate, "Phrasebook", tint = PrimaryNavy) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(
                    Brush.verticalGradient(listOf(GlobeBlue1.copy(alpha = 0.03f), Color.Transparent))
                )
        ) {
            // Language Selection
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip(label = "English", selected = selectedLang == "en", onClick = { selectedLang = "en" })
                CategoryChip(label = "Hindi", selected = selectedLang == "hi", onClick = { selectedLang = "hi" })
                CategoryChip(label = "Punjabi", selected = selectedLang == "pa", onClick = { selectedLang = "pa" })
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        onCopy = { clipboardManager.setText(AnnotatedString(msg.text)) },
                        onSpeak = { ttsManager.speak(msg.text, selectedLang) }
                    )
                }

                if (isThinking) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = LpuOrange1)
                            Spacer(Modifier.width(10.dp))
                            Text("GlobalBot is thinking...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
                        }
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(QUICK_PROMPTS) { prompt ->
                    SuggestionChip(
                        onClick = { sendMessage(prompt) },
                        label = { Text(prompt, fontSize = 12.sp) },
                        shape = RoundedCornerShape(20.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            }

            Surface(tonalElevation = 8.dp, shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val intent = createSpeechToTextIntent(selectedLang)
                        speechLauncher.launch(intent)
                    }) {
                        Icon(Icons.Default.Mic, "Voice Input", tint = LpuOrange1)
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type or speak in ${if(selectedLang=="en") "English" else if(selectedLang=="hi") "Hindi" else "Punjabi"}…") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange1)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { sendMessage(inputText) },
                        enabled = inputText.isNotBlank() && !isThinking,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank()) Brush.horizontalGradient(listOf(LpuOrange1, GlobeBlue))
                                else Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))
                            )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, onCopy: () -> Unit, onSpeak: () -> Unit) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Brush.linearGradient(listOf(LpuOrange1, GlobeBlue1))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp, topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp, bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) PrimaryNavy else MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.widthIn(max = 280.dp).clickable { onCopy() }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.text, fontSize = 14.sp, lineHeight = 20.sp,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isUser) {
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp, "Listen", tint = GlobeBlue1,
                            modifier = Modifier.size(18.dp).clickable { onSpeak() }
                        )
                    }
                }
            }
        }

        if (isUser) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(LpuOrange1.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = LpuOrange1, modifier = Modifier.size(18.dp))
            }
        }
    }
}

private fun generateMultilingualAiResponse(query: String, lang: String): String {
    val q = query.lowercase()
    return when (lang) {
        "hi" -> when {
            q.contains("frro") || q.contains("visa") -> "FRRO वीज़ा दिशा-निर्देश (LPU DIA):\n\n1. ब्लॉक 32 (अंतरराष्ट्रीय मामले प्रभाग) जाएं।\n2. पासपोर्ट, वीज़ा, Bonafide प्रमाण पत्र और C-Form लाएं।\n3. समाप्ति से 30 दिन पहले indianfrro.gov.in पर ऑनलाइन आवेदन करें।"
            q.contains("hostel") || q.contains("curfew") -> "LPU हॉस्टल नियम:\n\n• कर्फ्यू: रात 7:30 बजे (छात्राएं) और 9:00 बजे (छात्र)।\n• छुट्टी पास: UMS ऐप के माध्यम से 24 घंटे पहले आवेदन करें।"
            else -> "LPU ग्लोबल कैंपस में आपका स्वागत है! वीज़ा, ब्लॉक 32, हॉस्टल या परीक्षा के बारे में पूछें।"
        }
        "pa" -> when {
            q.contains("frro") || q.contains("visa") -> "FRRO ਵੀਜ਼ਾ ਦਿਸ਼ਾ-ਨਿਰਦੇਸ਼ (LPU DIA):\n\n1. ਬਲਾਕ 32 (DIA ਦਫ਼ਤਰ) ਜਾਓ।\n2. ਪਾਸਪੋਰਟ, ਵੀਜ਼ਾ ਅਤੇ C-Form ਨਾਲ ਲਿਆਓ।\n3. ਆਨਲਾਈਨ ਰਜਿਸਟ੍ਰੇਸ਼ਨ indianfrro.gov.in ਤੇ ਕਰੋ।"
            q.contains("hostel") || q.contains("curfew") -> "LPU ਹੋਸਟਲ ਨਿਯਮ:\n\n• ਕਰਫਿਊ: ਸ਼ਾਮ 7:30 (ਲੜਕੀਆਂ) & 9:00 (ਲੜਕੇ)।"
            else -> "LPU ਗਲੋਬਲ ਕੈਂਪਸ ਵਿੱਚ ਜੀ ਆਇਆਂ ਨੂੰ! ਵੀਜ਼ਾ, ਬਲਾਕ 32, ਜਾਂ ਹੋਸਟਲ ਬਾਰੇ ਪੁੱਛੋ।"
        }
        else -> when {
            q.contains("frro") || q.contains("visa") -> "FRRO Visa Extension Guidelines (LPU DIA):\n\n1. Visit Block 32 (Division of International Affairs).\n2. Bring original passport, current visa, Bonafide Certificate (UMS) and C-Form.\n3. Apply online at indianfrro.gov.in 30 days prior to expiry."
            q.contains("hostel") || q.contains("curfew") -> "LPU Hostel Rules & Timings:\n\n• Curfew: 7:30 PM (Girls) & 9:00 PM (Boys).\n• Leave Pass: Apply via UMS App 24 hours in advance."
            else -> "Welcome to LPU GlobalCampus AI! Ask me about FRRO visa, hostels, campus blocks, exams, or local living."
        }
    }
}