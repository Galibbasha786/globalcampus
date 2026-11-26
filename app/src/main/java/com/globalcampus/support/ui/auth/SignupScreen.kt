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
import com.globalcampus.support.ui.theme.PrimaryNavy
import kotlinx.coroutines.delay

@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onGoToLogin: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var regdNo by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var petName by remember { mutableStateOf("") }
    var favoriteTeacher by remember { mutableStateOf("") }
    var birthCity by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.success) {
        if (state.success) {
            vm.resetSuccess()
            onSignupSuccess()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "signupAnim")

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

    val bgPulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "bgPulse"
    )

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(GlobeBlue, LpuOrange, PrimaryNavy),
                        start = Offset(bgPulse * 600f, 0f),
                        end = Offset(bgPulse * 600f + 800f, 800f)
                    )
                )
                .graphicsLayer { alpha = 0.85f }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            StaggeredEntrance(delayMillis = 100) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(logoPulse)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(GlobeBlue.copy(alpha = glowAlpha), Color.Transparent)
                                )
                            )
                    )
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "GlobalCampus Logo",
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .graphicsLayer { translationY = floatOffset },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            StaggeredEntrance(delayMillis = 200) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Join GlobalCampus",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryNavy
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Fill registration details & security questions",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(Modifier.height(20.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; vm.clearError() },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = GlobeBlue) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regdNo,
                            onValueChange = { regdNo = it; vm.clearError() },
                            label = { Text("Registration Number (e.g. 12204859)") },
                            leadingIcon = { Icon(Icons.Default.Badge, null, tint = GlobeBlue) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it; vm.clearError() },
                            label = { Text("Home Country") },
                            placeholder = { Text("e.g. Nigeria, Vietnam, Nepal") },
                            leadingIcon = { Icon(Icons.Default.Public, null, tint = GlobeBlue) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; vm.clearError() },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = GlobeBlue) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; vm.clearError() },
                            label = { Text("Password (min 6 chars)") },
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
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(12.dp))

                        Text("Security Questions (For Password Recovery)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryNavy)
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = petName,
                            onValueChange = { petName = it; vm.clearError() },
                            label = { Text("Q1: What is your pet's name?") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = favoriteTeacher,
                            onValueChange = { favoriteTeacher = it; vm.clearError() },
                            label = { Text("Q2: What is your favorite teacher's name?") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = birthCity,
                            onValueChange = { birthCity = it; vm.clearError() },
                            label = { Text("Q3: What city were you born in?") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LpuOrange),
                            modifier = Modifier.fillMaxWidth()
                        )

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

                        Spacer(Modifier.height(24.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.horizontalGradient(listOf(GlobeBlue, LpuOrange)))
                        ) {
                            Button(
                                onClick = {
                                    vm.signUp(
                                        email = email,
                                        password = password,
                                        name = name,
                                        regdNo = regdNo,
                                        country = country,
                                        petName = petName,
                                        favoriteTeacher = favoriteTeacher,
                                        birthCity = birthCity
                                    )
                                },
                                enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 6 && regdNo.isNotBlank() && country.isNotBlank() && petName.isNotBlank() && !state.loading,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                if (state.loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Register & Save Profile", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        TextButton(onClick = onGoToLogin) {
                            Text(
                                text = "Already have an account? Sign in",
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
