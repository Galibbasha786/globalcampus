package com.globalcampus.support.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.globalcampus.support.ui.theme.AccentTeal
import com.globalcampus.support.ui.theme.PrimaryNavy
import com.globalcampus.support.ui.theme.SecondaryRoyalBlue

@Composable
fun StudentAvatar(
    avatarUrl: String,
    name: String = "",
    size: Dp = 44.dp,
    showOnlineStatus: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = modifier
                .background(
                    Brush.linearGradient(
                        listOf(PrimaryNavy, SecondaryRoyalBlue)
                    )
                )
                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when {
                avatarUrl.startsWith("http") -> {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                avatarUrl.isNotBlank() -> {
                    Text(
                        text = avatarUrl,
                        fontSize = (size.value * 0.45f).sp
                    )
                }
                else -> {
                    val initials = name.split(" ")
                        .take(2)
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .joinToString("")
                        .ifBlank { "🎓" }
                    Text(
                        text = initials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.38f).sp
                    )
                }
            }
        }

        if (showOnlineStatus) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .clip(CircleShape)
                    .background(AccentTeal)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}
