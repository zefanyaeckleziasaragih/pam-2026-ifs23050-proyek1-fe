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
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.AuthActionUIState
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel
) {
    val uiState by authViewModel.uiState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { authViewModel.resetRegisterState() }

    LaunchedEffect(uiState.authRegister) {
        if (!isLoading) return@LaunchedEffect
        when (val s = uiState.authRegister) {
            is AuthActionUIState.Success -> {
                isLoading = false
                navController.navigate(RouteHelper.LOGIN) {
                    popUpTo(RouteHelper.REGISTER) { inclusive = true }; launchSingleTop = true
                }
            }
            is AuthActionUIState.Error -> { isLoading = false; errorMessage = s.message }
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
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.TopCenter)
                .offset(y = 30.dp)
                .background(Brush.radialGradient(listOf(CinemaOrange.copy(0.12f), Color.Transparent)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(CinemaOrange.copy(0.2f), CinemaAmber.copy(0.08f))))
                    .border(2.dp, Brush.linearGradient(listOf(CinemaOrange.copy(0.8f), CinemaAmber.copy(0.4f))), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MovieCreation, null, tint = CinemaOrange, modifier = Modifier.size(40.dp))
            }

            Spacer(Modifier.height(16.dp))
            Text("WatchList", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp), color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text("Buat akun baru", style = MaterialTheme.typography.bodyMedium.copy(color = CinemaAmber.copy(0.7f), letterSpacing = 0.5.sp), textAlign = TextAlign.Center)
            Spacer(Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF2A1500), Color(0xFF1E0F00))))
                    .border(1.dp, Brush.verticalGradient(listOf(CinemaOrange.copy(0.5f), CinemaOrange.copy(0.1f))), RoundedCornerShape(24.dp))
            ) {
                RegisterForm(
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onClearError = { errorMessage = "" },
                    onRegister = { name, username, password ->
                        errorMessage = ""
                        when {
                            name.isBlank() -> { errorMessage = "Nama tidak boleh kosong"; return@RegisterForm }
                            username.isBlank() -> { errorMessage = "Username tidak boleh kosong"; return@RegisterForm }
                            password.isBlank() -> { errorMessage = "Password tidak boleh kosong"; return@RegisterForm }
                            password.length < 6 -> { errorMessage = "Password minimal 6 karakter"; return@RegisterForm }
                        }
                        isLoading = true
                        authViewModel.register(name, username, password)
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sudah punya akun? ", color = Color.White.copy(0.5f), fontSize = 14.sp)
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Masuk", color = CinemaAmber, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun RegisterForm(
    isLoading: Boolean,
    errorMessage: String,
    onClearError: () -> Unit,
    onRegister: (String, String, String) -> Unit
) {
    var name     by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    val (uFocus, pFocus) = remember { FocusRequester() to FocusRequester() }
    val focusMgr = LocalFocusManager.current

    Column(modifier = Modifier.padding(26.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Daftar", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 0.5.sp))

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

        OutlinedTextField(name, { name = it; onClearError() }, label = { Text("Nama Lengkap") }, leadingIcon = { Icon(Icons.Default.Badge, null, tint = CinemaOrange) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = cinemaTextFieldColors(), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { uFocus.requestFocus() }))

        OutlinedTextField(username, { username = it; onClearError() }, label = { Text("Username") }, leadingIcon = { Icon(Icons.Default.Person, null, tint = CinemaOrange) }, singleLine = true, modifier = Modifier.fillMaxWidth().focusRequester(uFocus), shape = RoundedCornerShape(14.dp), colors = cinemaTextFieldColors(), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { pFocus.requestFocus() }))

        OutlinedTextField(
            password, { password = it; onClearError() }, label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = CinemaOrange) },
            trailingIcon = { IconButton(onClick = { showPass = !showPass }) { Icon(if (showPass) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = Color.White.copy(0.4f)) } },
            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true, modifier = Modifier.fillMaxWidth().focusRequester(pFocus), shape = RoundedCornerShape(14.dp), colors = cinemaTextFieldColors(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { focusMgr.clearFocus(); onRegister(name, username, password) }),
            supportingText = {
                Text("Minimal 6 karakter",
                    color = if (password.isNotEmpty() && password.length < 6) Color(0xFFFF6B6B) else Color.White.copy(0.3f),
                    style = MaterialTheme.typography.labelSmall)
            }
        )

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (!isLoading) Brush.horizontalGradient(listOf(CinemaOrange, CinemaOrangeDeep))
                    else Brush.horizontalGradient(listOf(Color(0xFF5A3000), Color(0xFF5A3000)))
                )
        ) {
            Button(
                onClick = { onRegister(name, username, password) },
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Daftar", fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp)
            }
        }
    }
}