package com.globalcampus.support.ui.community

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.globalcampus.support.ui.components.CategoryChip
import com.globalcampus.support.ui.components.CommunityPostCard
import com.globalcampus.support.ui.components.EmptyStateView
import com.globalcampus.support.ui.components.LoadingSkeleton
import com.globalcampus.support.ui.components.PrimaryButton
import com.globalcampus.support.ui.theme.PrimaryNavy
import com.globalcampus.support.util.ImageUploader
import kotlinx.coroutines.launch

val LpuOrange = androidx.compose.ui.graphics.Color(0xFFF37021)
val GlobeBlue = androidx.compose.ui.graphics.Color(0xFF1E88E5)

@Composable
fun CommunityScreen(
    onOpenPost: (String) -> Unit,
    vm: CommunityViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            kotlinx.coroutines.delay(1500)
            vm.clearMessage()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreate = true },
                containerColor = LpuOrange,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                Icon(Icons.Default.Add, "Create Post")
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.categories) { category ->
                    CategoryChip(
                        label = category,
                        selected = state.selectedCategory == category,
                        onClick = { vm.setCategory(category) }
                    )
                }
            }

            AnimatedContent(
                targetState = state.loading,
                label = "communityLoading"
            ) { loading ->
                when {
                    loading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            repeat(4) { LoadingSkeleton() }
                        }
                    }
                    state.filtered.isEmpty() -> {
                        EmptyStateView(
                            title = "No Posts Yet",
                            description = "Be the first student to start a discussion in this category!",
                            icon = Icons.Default.Forum,
                            actionButton = {
                                PrimaryButton(text = "Create First Post", onClick = { showCreate = true })
                            }
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.filtered, key = { it.id }) { post ->
                                CommunityPostCard(
                                    post = post,
                                    myUid = vm.myUid,
                                    onClick = { onOpenPost(post.id) },
                                    onLike = { vm.toggleLike(post) },
                                    onDelete = { vm.deletePost(post.id) },
                                    onReshare = { vm.resharePost(post) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreatePostDialog(
            creating = state.creating,
            categories = state.categories.drop(1),
            onDismiss = { showCreate = false },
            onSubmit = { cat, title, body, imgUrl ->
                vm.createPost(cat, title, body, imgUrl)
                showCreate = false
            }
        )
    }
}

@Composable
private fun CreatePostDialog(
    creating: Boolean,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSubmit: (category: String, title: String, body: String, imageUrl: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.firstOrNull() ?: "General") }
    var isUploading by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isUploading = true
                val uploaded = ImageUploader.uploadImage(context, uri)
                isUploading = false
                if (!uploaded.isNullOrBlank()) imageUrl = uploaded
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Post", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryNavy) },
        text = {
            Column {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { c ->
                        CategoryChip(label = c, selected = category == c, onClick = { category = c })
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Post Title") }, singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = body, onValueChange = { body = it },
                    label = { Text("What's on your mind?") }, minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = { imagePicker.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = GlobeBlue.copy(0.12f), contentColor = GlobeBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = GlobeBlue)
                    } else {
                        Icon(Icons.Default.CloudUpload, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (imageUrl.isNotBlank()) "Photo Uploaded ✅" else "Attach Photo (Cloudinary)")
                    }
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Publish Post",
                onClick = { onSubmit(category, title.trim(), body.trim(), imageUrl.trim()) },
                enabled = !creating && !isUploading && title.isNotBlank() && body.isNotBlank(),
                loading = creating
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        shape = RoundedCornerShape(20.dp)
    )
}
