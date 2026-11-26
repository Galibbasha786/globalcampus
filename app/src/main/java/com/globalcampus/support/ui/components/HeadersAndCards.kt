package com.globalcampus.support.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.globalcampus.support.data.model.Post
import com.globalcampus.support.ui.theme.AccentTeal
import com.globalcampus.support.ui.theme.PrimaryNavy
import com.globalcampus.support.ui.theme.SecondaryRoyalBlue

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }
        }
        if (!actionText.isNullOrBlank() && onActionClick != null) {
            TextTextButton(text = actionText, onClick = onActionClick)
        }
    }
}

@Composable
private fun TextTextButton(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text = text, color = SecondaryRoyalBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun GradientHeroSection(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    actionButton: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(PrimaryNavy, SecondaryRoyalBlue)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            if (!badgeText.isNullOrBlank()) {
                Surface(
                    color = AccentTeal,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            if (actionButton != null) {
                Spacer(Modifier.height(14.dp))
                actionButton()
            }
        }
    }
}

@Composable
fun ResourceCategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = SecondaryRoyalBlue
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun CommunityPostCard(
    post: Post,
    myUid: String,
    onClick: () -> Unit,
    onLike: () -> Unit,
    onDelete: () -> Unit,
    onReshare: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val liked = post.likedBy.contains(myUid)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StudentAvatar(avatarUrl = "", name = post.authorName, size = 36.dp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        post.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryRoyalBlue
                    )
                }
                if (post.authorId == myUid) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                post.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                post.body,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            if (post.imageUrl.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                coil.compose.AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Attached image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLike, modifier = Modifier.size(28.dp)) {
                    Icon(
                        if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (liked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                }
                Spacer(Modifier.width(2.dp))
                Text("${post.likes}", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Spacer(Modifier.width(16.dp))
                Icon(
                    Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
                Spacer(Modifier.width(4.dp))
                Text("${post.commentCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                if (onReshare != null) {
                    Spacer(Modifier.width(16.dp))
                    IconButton(onClick = onReshare, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Reshare",
                            tint = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                    Text("${post.shares}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Inbox,
    actionButton: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(SecondaryRoyalBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = SecondaryRoyalBlue,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            description,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (actionButton != null) {
            Spacer(Modifier.height(16.dp))
            actionButton()
        }
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Retry", color = Color.White)
            }
        }
    }
}

@Composable
fun LoadingSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
        )
    ) {}
}
