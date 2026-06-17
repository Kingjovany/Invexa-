package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.InvestmentViewModel

@Composable
fun AuthScreen(
    viewModel: InvestmentViewModel,
    onAuthSuccess: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    
    // Inputs
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var referralBy by remember { mutableStateOf("") }
    
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val uiMessage by viewModel.uiMessage.collectAsState()

    // Premium background gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Breathing infinite transition for premium branding feel
            val infiniteTransition = rememberInfiniteTransition(label = "invexa_icon_pulse")
            val iconScale by infiniteTransition.animateFloat(
                initialValue = 0.94f,
                targetValue = 1.04f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "invexa_scale"
            )
            val iconRotation by infiniteTransition.animateFloat(
                initialValue = -1.5f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "invexa_rotation"
            )

            // Logo Icon displaying Invexa Premium Brand Asset
            Card(
                modifier = Modifier
                    .size(92.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        rotationZ = iconRotation
                    },
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                )
            ) {
                Image(
                    painter = painterResource(id = com.example.R.drawable.app_icon_invexa_1781579473931),
                    contentDescription = "Invexa App Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Brand Header Switch
            AnimatedContent(
                targetState = isLoginMode,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(250)) + slideInVertically()).togetherWith(
                        fadeOut(animationSpec = tween(150)) + slideOutVertically()
                    )
                },
                label = "brand_header_transition"
            ) { targetMode ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (targetMode) "Welcome to Invexa" else "Create Portfolio Account",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (targetMode) "Manage products, track earnings & invest securely" else "Receive 5% affiliate commissions on referral deposits",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main input card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                AnimatedContent(
                    targetState = isLoginMode,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, delayMillis = 100)) + slideInVertically(animationSpec = tween(220, delayMillis = 100)) { height -> height / 12 }).togetherWith(
                            fadeOut(animationSpec = tween(100)) + slideOutVertically(animationSpec = tween(100)) { height -> -height / 12 }
                        )
                    },
                    label = "auth_form_transition"
                ) { targetLoginMode ->
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (targetLoginMode) {
                            // Email input for login
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Email") },
                                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("username_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        } else {
                            // Registration fields
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = "Name") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("name_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Username") },
                                leadingIcon = { Icon(Icons.Outlined.AlternateEmail, contentDescription = "Username") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("username_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password") },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        if (!targetLoginMode) {
                            // Referral Code (Optional)
                            OutlinedTextField(
                                value = referralBy,
                                onValueChange = { referralBy = it },
                                label = { Text("Referral Code (Optional)") },
                                leadingIcon = { Icon(Icons.Outlined.GroupAdd, contentDescription = "Referral") },
                                placeholder = { Text("e.g. SYSTEM100") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("referral_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        // API Call Feedback
                        if (uiMessage != null) {
                            Text(
                                text = uiMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Primary Button
                        Button(
                            onClick = {
                                viewModel.clearMessage()
                                isLoading = true
                                if (targetLoginMode) {
                                    viewModel.login(username, password) { success ->
                                        isLoading = false
                                        if (success) onAuthSuccess()
                                    }
                                } else {
                                    viewModel.register(username, email, fullName, password, referralBy) { success ->
                                        isLoading = false
                                        if (success) {
                                            // Auto log in after registering successfully
                                            viewModel.login(username, password) { loginSuccess ->
                                                if (loginSuccess) onAuthSuccess()
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("submit_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = if (targetLoginMode) "Log In Securely" else "Register & Create Wallet",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Sandbox login shortcuts for seamless review
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    username = "admin"
                                    password = "admin"
                                },
                                modifier = Modifier.testTag("shortcut_admin")
                            ) {
                                Text(
                                    text = if (targetLoginMode) "Autofill Admin Login" else "Autofill Admin Demo",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                            
                            TextButton(
                                onClick = {
                                    isLoginMode = !isLoginMode
                                    viewModel.clearMessage()
                                },
                                modifier = Modifier.testTag("toggle_auth_mode")
                            ) {
                                Text(
                                    text = if (targetLoginMode) "Create Account" else "Back to Login",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Compliance Disclaimers
            Text(
                text = "Disclaimer: Invexa does not guarantee fixed earnings. Asset valuations fluctuate based on economic trends. Past performance does not assure future gains. Invest responsibly.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
