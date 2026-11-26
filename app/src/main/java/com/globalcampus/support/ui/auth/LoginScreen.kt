package com.globalcampus.support.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.globalcampus.support.R
import com.globalcampus.support.ui.components.PrimaryButton
import com.globalcampus.support.ui.theme.PrimaryNavy
import com.globalcampus.support.ui.theme.SecondaryRoyalBlue
import kotlinx.coroutines.delay

// Brand Colors matching HomeScreen
val LpuOrange = Color(0xFFF37021)
val GlobeBlue = Color(0xFF1E88E5)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToSignup: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.success) {
        if (state.success) {
            vm.resetSuccess()
            onLoginSuccess()
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        contentVisible = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "authAnim")

    // Logo Animations
    val logoPulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "logoPulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glowAlpha"
    )
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(2400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "floatOffset"
    )

    // Background Gradient Animation
    val bgPulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "bgPulse"
    )

    // Reusable staggered entrance helper
    @Composable
    fun StaggeredEntrance(delayMillis: Int, content: @Composable () -> Unit) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(delayMillis.toLong())
            visible = true
        }
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow)
            )
        ) {
            content()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Animated Floating Background Orbs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(LpuOrange, GlobeBlue, PrimaryNavy),
                        start = Offset(bgPulse * 600f, 0f),
                        end = Offset(bgPulse * 600f + 800f, 800f)
                    )
                )
                .graphicsLayer { alpha = 0.85f }
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp, y = 150.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = 250.dp, y = 50.dp)
                .background(LpuOrange.copy(alpha = 0.15f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo Container
            StaggeredEntrance(delayMillis = 100) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                    // Outer Glow
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(logoPulse)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(LpuOrange.copy(alpha = glowAlpha), Color.Transparent)
                                )
                            )
                    )
                    // Logo Image
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "GlobalCampus Logo",
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(22.dp)) // Matches your logo shape
                            .graphicsLayer { translationY = floatOffset }
                            .scale(logoPulse),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Login Card
            StaggeredEntrance(delayMillis = 300) {
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Welcome Back",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryNavy
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Sign in to continue to LPU GlobalCampus",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(Modifier.height(32.dp))

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; vm.clearError() },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = GlobeBlue) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LpuOrange,
                                focusedLabelColor = LpuOrange,
                                cursorColor = LpuOrange
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; vm.clearError() },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = GlobeBlue) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password",
                                        tint = GlobeBlue
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LpuOrange,
                                focusedLabelColor = LpuOrange,
                                cursorColor = LpuOrange
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        var showForgotPasswordDialog by remember { mutableStateOf(false) }

                        TextButton(
                            onClick = { showForgotPasswordDialog = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = "Forgot Password?",
                                color = GlobeBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        if (showForgotPasswordDialog) {
                            var resetEmail by remember { mutableStateOf(email) }
                            var petAnswer by remember { mutableStateOf("") }
                            var teacherAnswer by remember { mutableStateOf("") }
                            var cityAnswer by remember { mutableStateOf("") }
                            var newPasswordInput by remember { mutableStateOf("") }

                            AlertDialog(
                                onDismissRequest = { showForgotPasswordDialog = false },
                                title = { Text(if (state.answersVerified) "Set New Password" else "Reset Password via Security Questions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryNavy) },
                                text = {
                                    Column {
                                        if (!state.answersVerified) {
                                            Text("Answer your security questions to reset password.", fontSize = 12.sp, color = Color.Gray)
                                            Spacer(Modifier.height(10.dp))
                                            OutlinedTextField(
                                                value = resetEmail,
                                                onValueChange = { resetEmail = it },
                                                label = { Text("Your Registered Email") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = petAnswer,
                                                onValueChange = { petAnswer = it },
                                                label = { Text("Q1: What is your pet's name?") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = teacherAnswer,
                                                onValueChange = { teacherAnswer = it },
                                                label = { Text("Q2: Favorite teacher's name?") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = cityAnswer,
                                                onValueChange = { cityAnswer = it },
                                                label = { Text("Q3: City you were born in?") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        } else {
                                            Surface(
                                                color = Color(0xFF10B981).copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("✅ Answers Verified! Enter your new password below:", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                                            }
                                            Spacer(Modifier.height(12.dp))
                                            OutlinedTextField(
                                                value = newPasswordInput,
                                                onValueChange = { newPasswordInput = it },
                                                label = { Text("Enter New Password (min 6 chars)") },
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        if (state.message != null) {
                                            Spacer(Modifier.height(8.dp))
                                            Text(state.message!!, color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        if (state.error != null) {
                                            Spacer(Modifier.height(8.dp))
                                            Text(state.error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                        }
                                    }
                                },
                                confirmButton = {
                                    if (!state.answersVerified) {
                                        Button(
                                            onClick = {
                                                vm.verifySecurityAnswers(resetEmail, petAnswer, teacherAnswer, cityAnswer)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = LpuOrange),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Verify Security Answers", fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                vm.resetPasswordAndSignIn(resetEmail, newPasswordInput)
                                                showForgotPasswordDialog = false
                                            },
                                            enabled = newPasswordInput.length >= 6,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Save New Password & Sign In", fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                                        Text("Cancel", color = Color.Gray)
                                    }
                                },
                                shape = RoundedCornerShape(20.dp),
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        }

                        // Animated Error Message
                        AnimatedVisibility(
                            visible = state.error != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            state.error?.let {
                                Spacer(Modifier.height(12.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = it,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(28.dp))

                        // Primary Button with custom brand gradient
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.horizontalGradient(listOf(LpuOrange, GlobeBlue)))
                        ) {
                            Button(
                                onClick = { vm.signIn(email, password) },
                                enabled = email.isNotBlank() && password.isNotBlank() && !state.loading,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                if (state.loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        TextButton(onClick = onGoToSignup) {
                            Text(
                                text = "Don't have an account? Sign up",
                                color = GlobeBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}