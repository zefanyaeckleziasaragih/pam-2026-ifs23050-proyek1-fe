package org.delcom.pam_ifs23050_proyek1.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.delcom.pam_ifs23050_proyek1.helper.RouteHelper
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaAmber
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaOrange
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaOrangeDeep
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.AuthUIState
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel
) {
    val uiState by authViewModel.uiState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(uiState.auth) {
        if (!isLoading) return@LaunchedEffect
        when (val s = uiState.auth) {
            is AuthUIState.Error -> { isLoading = false; errorMessage = s.message }
            is AuthUIState.Success -> { /* NavHost handles redirect */ }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D0800), Color(0xFF1A0F00), Color(0xFF261200))
                )
            )
    ) {
        // Decorative radial glow behind logo
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopCenter)
                .offset(y = 40.dp)
                .background(
                    Brush.radialGradient(
                        listOf(CinemaOrange.copy(0.15f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // Logo area
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(CinemaOrange.copy(0.25f), CinemaAmber.copy(0.1f))
                        )
                    )
                    .border(
                        2.dp,
                        Brush.linearGradient(listOf(CinemaOrange.copy(0.8f), CinemaAmber.copy(0.4f))),
                        RoundedCornerShape(26.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = CinemaOrange,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "WatchList",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Catat perjalanan sinematikmu",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = CinemaAmber.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(44.dp))

            // Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2A1500), Color(0xFF1E0F00))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(CinemaOrange.copy(0.5f), CinemaOrange.copy(0.1f))
                        ),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                LoginForm(
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onClearError = { errorMessage = "" },
                    onLogin = { u, p ->
                        errorMessage = ""
                        when {
                            u.isBlank() -> { errorMessage = "Username tidak boleh kosong"; return@LoginForm }
                            p.isBlank() -> { errorMessage = "Password tidak boleh kosong"; return@LoginForm }
                        }
                        isLoading = true
                        authViewModel.login(u, p)
                    }
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Belum punya akun? ", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                TextButton(onClick = {
                    navController.navigate(RouteHelper.REGISTER) { launchSingleTop = true }
                }) {
                    Text(
                        "Daftar",
                        color = CinemaAmber,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun LoginForm(
    isLoading: Boolean,
    errorMessage: String,
    onClearError: () -> Unit,
    onLogin: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    val passFocus = remember { FocusRequester() }
    val focusMgr = LocalFocusManager.current

    Column(modifier = Modifier.padding(26.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Masuk",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
        )

        if (errorMessage.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF3D1010))
                    .border(1.dp, Color(0xFFFF6B6B).copy(0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(16.dp))
                Text(errorMessage, color = Color(0xFFFF6B6B), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                IconButton(onClick = onClearError, modifier = Modifier.size(18.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(12.dp))
                }
            }
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; onClearError() },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Default.Person, null, tint = CinemaOrange) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = cinemaTextFieldColors(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { passFocus.requestFocus() })
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; onClearError() },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = CinemaOrange) },
            trailingIcon = {
                IconButton(onClick = { showPass = !showPass }) {
                    Icon(
                        if (showPass) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        null,
                        tint = Color.White.copy(0.4f)
                    )
                }
            },
            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(passFocus),
            shape = RoundedCornerShape(14.dp),
            colors = cinemaTextFieldColors(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { focusMgr.clearFocus(); onLogin(username, password) })
        )

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (!isLoading) Brush.horizontalGradient(
                        listOf(CinemaOrange, CinemaOrangeDeep)
                    ) else Brush.horizontalGradient(
                        listOf(Color(0xFF5A3000), Color(0xFF5A3000))
                    )
                )
        ) {
            Button(
                onClick = { onLogin(username, password) },
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Masuk", fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun cinemaTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White.copy(0.8f),
    focusedBorderColor = CinemaOrange,
    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
    cursorColor = CinemaAmber,
    focusedLabelColor = CinemaOrange,
    unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
    focusedContainerColor = Color.White.copy(0.04f),
    unfocusedContainerColor = Color.White.copy(0.02f),
    focusedLeadingIconColor = CinemaOrange,
    unfocusedLeadingIconColor = CinemaOrange.copy(0.6f)
)