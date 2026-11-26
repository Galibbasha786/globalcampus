package com.globalcampus.support.ui.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
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
import com.globalcampus.support.data.model.Article
import com.globalcampus.support.data.repository.ArticleRepository
import com.globalcampus.support.ui.theme.SecondaryRoyalBlue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArticleDetailViewModel : ViewModel() {
    private val repo = ArticleRepository()
    private val _article = MutableStateFlow<Article?>(null)
    val article: StateFlow<Article?> = _article
    private val _bookmarked = MutableStateFlow(false)
    val bookmarked: StateFlow<Boolean> = _bookmarked
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun load(id: String) {
        viewModelScope.launch {
            _article.value = repo.getArticle(id).getOrNull()
            _bookmarked.value = repo.getBookmarkedIds().getOrDefault(emptySet()).contains(id)
            _loading.value = false
        }
    }
    fun toggleBookmark(id: String) {
        viewModelScope.launch {
            val now = !_bookmarked.value
            repo.toggleBookmark(id, now)
            _bookmarked.value = now
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleId: String,
    onBack: () -> Unit,
    vm: ArticleDetailViewModel = viewModel()
) {
    LaunchedEffect(articleId) { vm.load(articleId) }
    val article by vm.article.collectAsStateWithLifecycle()
    val bookmarked by vm.bookmarked.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campus Guide", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.toggleBookmark(articleId) }) {
                        Icon(
                            if (bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            "Bookmark",
                            tint = if (bookmarked) SecondaryRoyalBlue else MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SecondaryRoyalBlue)
            }
            return@Scaffold
        }
        val a = article ?: return@Scaffold
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Surface(
                color = SecondaryRoyalBlue.copy(alpha = 0.12f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    a.category.uppercase(),
                    fontSize = 11.sp,
                    color = SecondaryRoyalBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(a.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
            Spacer(Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(a.body, fontSize = 15.sp, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.85f))
                    if (a.tags.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text("Tags & Key Terms", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            a.tags.joinToString("  ") { "#$it" },
                            fontSize = 13.sp,
                            color = SecondaryRoyalBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
