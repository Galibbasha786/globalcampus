package com.globalcampus.support.ui.tools

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.globalcampus.support.ui.theme.SecondaryRoyalBlue
import kotlinx.coroutines.launch

data class Phrase(
    val english: String,
    val hindi: String,
    val punjabi: String,
    val phonetics: String,
    val category: String
)

val PHRASES = listOf(
    Phrase("Hello / Greetings", "Namaste", "Sat Sri Akal", "nah-mas-tay / sat sree ah-kahl", "Essentials"),
    Phrase("How much to Block 32?", "Block 32 ka kitna hoga?", "Block 32 da kinna hoyga?", "Block 32 kay kit-na ho-ga", "Transport"),
    Phrase("Please reduce the fare", "Thoda kam karo bhaiya", "Thoda ghatt karo bhaji", "tho-da kam ka-ro bhai-ya", "Transport"),
    Phrase("Where is the Uni-Hospital?", "Uni-Hospital kahan hai?", "Uni-Hospital kithay hai?", "Uni-Hospital ka-han hai", "Campus"),
    Phrase("Is this vegetarian food?", "Kya yeh shuddh shakahari hai?", "Ki eh shuddh shakahari hai?", "kya yeh shak-a-ha-ri hai", "Food"),
    Phrase("Less spicy please", "Kam mirch rakhna", "Ghatt mirch rakhna", "kam mirch rakh-na", "Food"),
    Phrase("Thank you very much", "Bahut bahut dhanyavaad", "Bohot bohot dhanbaad", "bah-ut dhanya-vaad", "Essentials"),
    Phrase("Can you help me please?", "Kya aap meri madad kar sakte hain?", "Ki tussi meri madad kar sakde ho?", "ma-dad kar sak-tay hain", "Emergency")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhrasebookScreen(onBack: () -> Unit) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Essentials", "Transport", "Campus", "Food", "Emergency")
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filteredPhrases = if (selectedCategory == "All") PHRASES else PHRASES.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🗣️ Campus Phrasebook", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp)
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredPhrases) { phrase ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                clipboardManager.setText(AnnotatedString("${phrase.hindi} / ${phrase.punjabi}"))
                                scope.launch { snackbarHostState.showSnackbar("Copied phrase!") }
                            }
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    phrase.english,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    Icons.Default.VolumeUp,
                                    contentDescription = "Pronounce",
                                    tint = SecondaryRoyalBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("🇮🇳 Hindi: ${phrase.hindi}", fontSize = 12.sp) }
                                )
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("🌾 Punjabi: ${phrase.punjabi}", fontSize = 12.sp) }
                                )
                            }
                            Text(
                                "🗣️ Phonetic: ${phrase.phonetics}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}