package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BiometricAuthDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.FitnessViewModel

enum class AuthMode {
    LOGIN,
    SIGNUP
}

@Composable
fun AuthScreen(
    viewModel: FitnessViewModel,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val focusManager = LocalFocusManager.current

    // Sign in fields
    var loginEmail by remember { mutableStateOf("alex.fit@apexfit.io") }
    var loginPassword by remember { mutableStateOf("apexfit123") }
    var isLoginPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    // Sign up fields
    var signupName by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var signupConfirmPassword by remember { mutableStateOf("") }
    var isSignupPasswordVisible by remember { mutableStateOf(false) }
    var selectedGoal by remember { mutableStateOf("Muscle Growth & Hypertrophy") }
    var selectedLevel by remember { mutableStateOf("Intermediate") }

    // Dialog state
    var showBiometricDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }
    var authSuccessNotice by remember { mutableStateOf<String?>(null) }

    val fitnessGoals = listOf(
        "Muscle Growth & Hypertrophy",
        "Fat Loss & Conditioning",
        "Athletic Performance & Speed",
        "Endurance & Longevity"
    )

    val fitnessLevels = listOf("Beginner", "Intermediate", "Advanced", "Elite")

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("auth_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            // 1. Branding Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(listOf(NeonCyan, RoyalPurple))
                            )
                            .border(
                                2.dp,
                                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)),
                                RoundedCornerShape(22.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FitnessCenter,
                            contentDescription = "ApexFit Logo",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "APEXFIT",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Smart Athletic Intelligence & Daily Conditioning",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 2. Mode Selector (Sign In vs Create Account)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BentoSurface,
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (authMode == AuthMode.LOGIN) BentoPrimaryHighlight else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    authMode = AuthMode.LOGIN
                                    authErrorMessage = null
                                }
                                .testTag("auth_tab_login")
                        ) {
                            Text(
                                text = "Sign In",
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = if (authMode == AuthMode.LOGIN) FontWeight.Bold else FontWeight.Medium,
                                color = if (authMode == AuthMode.LOGIN) BentoPrimaryDark else BentoTextSecondary,
                                fontSize = 14.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (authMode == AuthMode.SIGNUP) BentoPrimaryHighlight else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    authMode = AuthMode.SIGNUP
                                    authErrorMessage = null
                                }
                                .testTag("auth_tab_signup")
                        ) {
                            Text(
                                text = "Create Account",
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = if (authMode == AuthMode.SIGNUP) FontWeight.Bold else FontWeight.Medium,
                                color = if (authMode == AuthMode.SIGNUP) BentoPrimaryDark else BentoTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 3. Error Banner (if any)
            if (authErrorMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CrimsonRed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = "Error",
                                tint = CrimsonRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = authErrorMessage!!,
                                color = CrimsonRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 4. Form Container
            item {
                AnimatedContent(
                    targetState = authMode,
                    label = "auth_form_anim"
                ) { mode ->
                    when (mode) {
                        AuthMode.LOGIN -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Email Field
                                OutlinedTextField(
                                    value = loginEmail,
                                    onValueChange = {
                                        loginEmail = it
                                        authErrorMessage = null
                                    },
                                    label = { Text("Email Address") },
                                    placeholder = { Text("athlete@apexfit.io") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Email,
                                            contentDescription = "Email",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        if (loginEmail.isNotEmpty()) {
                                            IconButton(onClick = { loginEmail = "" }) {
                                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_login_email")
                                )

                                // Password Field
                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = {
                                        loginPassword = it
                                        authErrorMessage = null
                                    },
                                    label = { Text("Password") },
                                    placeholder = { Text("••••••••") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = "Password",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { isLoginPasswordVisible = !isLoginPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isLoginPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = "Toggle password"
                                            )
                                        }
                                    },
                                    visualTransformation = if (isLoginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            viewModel.login(loginEmail, loginPassword) { success, err ->
                                                if (success) onAuthSuccess() else authErrorMessage = err
                                            }
                                        }
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_login_password")
                                )

                                // Options: Remember Me & Forgot Password
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { rememberMe = !rememberMe }
                                    ) {
                                        Checkbox(
                                            checked = rememberMe,
                                            onCheckedChange = { rememberMe = it },
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Remember me",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            forgotPasswordEmail = loginEmail
                                            showForgotPasswordDialog = true
                                        }
                                    ) {
                                        Text(
                                            text = "Forgot password?",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Sign In Button
                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.login(loginEmail, loginPassword) { success, err ->
                                            if (success) {
                                                onAuthSuccess()
                                            } else {
                                                authErrorMessage = err
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("btn_auth_submit_login"),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = !isAuthLoading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    if (isAuthLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.5.dp
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Sign In to ApexFit",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Icon(
                                                imageVector = Icons.Filled.ArrowForward,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                // 2. Biometric Sign-In (Fingerprint / Face ID) Card
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = BentoSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        Brush.horizontalGradient(listOf(NeonCyan, NeonLime))
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showBiometricDialog = true
                                        }
                                        .testTag("btn_biometric_login")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(NeonCyan.copy(alpha = 0.25f), NeonLime.copy(alpha = 0.25f))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Fingerprint,
                                                    contentDescription = "Biometric Auth",
                                                    tint = NeonCyan,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "Sign in with Biometrics",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = NeonLime.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = "SECURE",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = NeonLime,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Fingerprint or Facial Recognition Unlock",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Filled.Face,
                                            contentDescription = "Face ID",
                                            tint = NeonLime,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Quick Demo Account Sign-In Card
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = NeonCyan.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.quickDemoLogin {
                                                onAuthSuccess()
                                            }
                                        }
                                        .testTag("btn_quick_demo_login")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(NeonCyan.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Bolt,
                                                contentDescription = "Demo Account",
                                                tint = NeonCyan,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "1-Tap Demo Access",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Instant login as Alex Rivers (Preloaded Workouts & Sensors)",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Filled.ChevronRight,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        AuthMode.SIGNUP -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Name Field
                                OutlinedTextField(
                                    value = signupName,
                                    onValueChange = {
                                        signupName = it
                                        authErrorMessage = null
                                    },
                                    label = { Text("Full Name") },
                                    placeholder = { Text("e.g. Jordan Hayes") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Person,
                                            contentDescription = "Name",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_signup_name")
                                )

                                // Email Field
                                OutlinedTextField(
                                    value = signupEmail,
                                    onValueChange = {
                                        signupEmail = it
                                        authErrorMessage = null
                                    },
                                    label = { Text("Email Address") },
                                    placeholder = { Text("you@example.com") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Email,
                                            contentDescription = "Email",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_signup_email")
                                )

                                // Password Field
                                OutlinedTextField(
                                    value = signupPassword,
                                    onValueChange = {
                                        signupPassword = it
                                        authErrorMessage = null
                                    },
                                    label = { Text("Password (min 6 characters)") },
                                    placeholder = { Text("••••••••") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = "Password",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { isSignupPasswordVisible = !isSignupPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isSignupPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = "Toggle password"
                                            )
                                        }
                                    },
                                    visualTransformation = if (isSignupPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Next
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_signup_password")
                                )

                                // Confirm Password Field
                                OutlinedTextField(
                                    value = signupConfirmPassword,
                                    onValueChange = {
                                        signupConfirmPassword = it
                                        authErrorMessage = null
                                    },
                                    label = { Text("Confirm Password") },
                                    placeholder = { Text("••••••••") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.LockClock,
                                            contentDescription = "Confirm Password",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    visualTransformation = if (isSignupPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    isError = signupConfirmPassword.isNotEmpty() && signupConfirmPassword != signupPassword,
                                    supportingText = {
                                        if (signupConfirmPassword.isNotEmpty() && signupConfirmPassword != signupPassword) {
                                            Text("Passwords do not match", color = CrimsonRed, fontSize = 11.sp)
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_signup_confirm_password")
                                )

                                // Fitness Goal Selection
                                Text(
                                    text = "Primary Training Goal",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    fitnessGoals.forEach { goal ->
                                        val isSelected = selectedGoal == goal
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else BentoSurface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else BentoOutline
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedGoal = goal }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = goal,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Filled.CheckCircle,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Fitness Level Selector Chips
                                Text(
                                    text = "Experience Level",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    fitnessLevels.forEach { lvl ->
                                        val isSelected = selectedLevel == lvl
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else BentoSurface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else BentoOutline
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedLevel = lvl }
                                        ) {
                                            Text(
                                                text = lvl,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Sign Up Submit Button
                                Button(
                                    onClick = {
                                        if (signupPassword != signupConfirmPassword) {
                                            authErrorMessage = "Passwords do not match."
                                            return@Button
                                        }
                                        focusManager.clearFocus()
                                        viewModel.register(
                                            name = signupName,
                                            email = signupEmail,
                                            pass = signupPassword,
                                            goal = selectedGoal,
                                            level = selectedLevel
                                        ) { success, err ->
                                            if (success) {
                                                onAuthSuccess()
                                            } else {
                                                authErrorMessage = err
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("btn_auth_submit_signup"),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = !isAuthLoading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    if (isAuthLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.5.dp
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Create Free Account",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Icon(
                                                imageVector = Icons.Filled.FitnessCenter,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Social Authentication Divider
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BentoOutline)
                    Text(
                        text = "  OR CONTINUE WITH  ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BentoOutline)
                }
            }

            // 6. Fast One-Tap Providers (Google / Apple)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.register(
                                name = "Google Athlete",
                                email = "athlete.google@apexfit.io",
                                pass = "google_auth_token",
                                goal = "Athletic Performance & Speed",
                                level = "Intermediate"
                            ) { success, _ ->
                                if (success) onAuthSuccess()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_social_google"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = BentoSurface)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Google",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Google", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.register(
                                name = "Apple Athlete",
                                email = "athlete.apple@apexfit.io",
                                pass = "apple_auth_token",
                                goal = "Muscle Growth & Hypertrophy",
                                level = "Advanced"
                            ) { success, _ ->
                                if (success) onAuthSuccess()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_social_apple"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = BentoSurface)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Apple",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Apple", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 7. Terms & Privacy Footnote
            item {
                Text(
                    text = "By continuing, you agree to ApexFit's Terms of Service and Privacy Policy. Secure encrypted cloud biometric sync enabled.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
            }
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LockReset,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Reset Password", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Enter the email associated with your ApexFit account. We will send you a secure verification link to create a new password.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.requestPasswordReset(forgotPasswordEmail) { ok, msg ->
                            showForgotPasswordDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showBiometricDialog) {
        BiometricAuthDialog(
            userEmail = loginEmail,
            userName = "Alex Rivers",
            onDismiss = { showBiometricDialog = false },
            onSuccess = {
                showBiometricDialog = false
                viewModel.loginWithBiometrics { success, _ ->
                    if (success) {
                        onAuthSuccess()
                    }
                }
            },
            onUsePassword = {
                showBiometricDialog = false
            }
        )
    }
}
