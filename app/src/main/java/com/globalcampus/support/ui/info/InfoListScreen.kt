package com.globalcampus.support.ui.info

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.globalcampus.support.ui.components.CategoryChip
import com.globalcampus.support.ui.components.EmptyStateView
import com.globalcampus.support.ui.components.PrimaryButton
import com.globalcampus.support.ui.components.SearchField
import com.globalcampus.support.ui.theme.SecondaryRoyalBlue

private val CATEGORIES = listOf("All", "Visa", "Banking", "Housing", "Health", "Campus",
    "Transport", "Work", "Finance", "Social", "Safety", "Travel")

@Composable
fun InfoListScreen(
    onOpenArticle: (String) -> Unit,
    vm: InfoViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.seedMessage) {
        state.seedMessage?.let {
            kotlinx.coroutines.delay(2500)
            vm.clearSeedMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(Modifier.height(8.dp))

        // Search bar
        SearchField(
            query = state.query,
            onQueryChange = vm::setQuery,
            placeholder = "Search guides, visa rules, housing...",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(10.dp))

        // Categories
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CATEGORIES) { cat ->
                CategoryChip(
                    label = cat,
                    selected = state.selectedCategory == cat,
                    onClick = { vm.setCategory(cat) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Seed banner
        state.seedMessage?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(it, Modifier.padding(12.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (state.articles.isEmpty() && !state.seeding) {
            EmptyStateView(
                title = "No Articles Found",
                description = "Seed the campus directory with 20+ comprehensive guide articles for international students.",
                icon = Icons.Default.MenuBook,
                actionButton = {
                    PrimaryButton(
                        text = "🌱 Seed LPU Directory Articles",
                        onClick = vm::seed,
                        loading = state.seeding
                    )
                }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.filtered, key = { it.id }) { article ->
                    val saved = state.bookmarks.contains(article.id)
                    ArticleCard(
                        title = article.title,
                        category = article.category,
                        preview = article.body.take(120),
                        saved = saved,
                        onClick = { onOpenArticle(article.id) },
                        onToggleSave = { vm.toggleBookmark(article.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(
    title: String,
    category: String,
    preview: String,
    saved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = SecondaryRoyalBlue.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        category.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryRoyalBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onToggleSave) {
                    Icon(
                        if (saved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (saved) SecondaryRoyalBlue else MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(
                preview,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(0.65f),
                lineHeight = 18.sp
            )
        }
    }
}
