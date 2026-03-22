package org.delcom.pam_ifs23050_proyek1.ui.screens.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.CachePolicy
import coil.request.ImageRequest
import org.delcom.pam_ifs23050_proyek1.BuildConfig
import org.delcom.pam_ifs23050_proyek1.helper.ImageCompressHelper
import org.delcom.pam_ifs23050_proyek1.helper.RouteHelper
import org.delcom.pam_ifs23050_proyek1.ui.components.BottomNavComponent
import org.delcom.pam_ifs23050_proyek1.ui.components.WatchListTopBar
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaAmber
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaOrange
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaOrangeDeep
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.*

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authToken: String,
    movieViewModel: MovieViewModel,
    authViewModel: AuthViewModel
) {
    if (authToken.isBlank()) {
        LaunchedEffect(Unit) { navController.navigate(RouteHelper.LOGIN) { popUpTo(0) { inclusive = true } } }
        return
    }

    val uiState     by movieViewModel.uiState.collectAsState()
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val context     = LocalContext.current
    val snackbar    = remember { SnackbarHostState() }
    var photoTs     by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var pendingUri  by remember { mutableStateOf<Uri?>(null) }
    var showPhotoDialog  by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditSheet    by remember { mutableStateOf(false) }
    var showPwSheet      by remember { mutableStateOf(false) }
    var showAboutSheet   by remember { mutableStateOf(false) }
    var logoutTriggered  by remember { mutableStateOf(false) }

    val profile = (uiState.profile as? ProfileUIState.Success)?.data

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { pendingUri = it; showPhotoDialog = true }
    }

    LaunchedEffect(Unit) { movieViewModel.getProfile(authToken) }

    LaunchedEffect(uiState.profilePhoto) {
        when (val s = uiState.profilePhoto) {
            is MovieActionUIState.Success -> {
                photoTs = System.currentTimeMillis()
                pendingUri = null
                movieViewModel.resetProfilePhotoState()
                movieViewModel.getProfile(authToken)
            }
            is MovieActionUIState.Error -> {
                snackbar.showSnackbar("error|${s.message}")
                movieViewModel.resetProfilePhotoState()
            }
            else -> {}
        }
    }

    LaunchedEffect(uiStateAuth.authLogout) {
        if (!logoutTriggered) return@LaunchedEffect
        when (uiStateAuth.authLogout) {
            is AuthLogoutUIState.Success, is AuthLogoutUIState.Error ->
                navController.navigate(RouteHelper.LOGIN) { popUpTo(0) { inclusive = true } }
            else -> {}
        }
    }

    // FIX: Column layout so BottomNav doesn't overlap content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0800))
    ) {
        WatchListTopBar(title = "Profil", showBackButton = false, navController = navController)

        // FIX: weight(1f) + SnackbarHost inside Box
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Avatar section ─────────────────────────────────────────────
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .size(110.dp)
                        .clickable { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                ) {
                    val profileId = profile?.id
                    key(photoTs) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2A1500))
                                .border(
                                    2.dp,
                                    Brush.sweepGradient(listOf(CinemaOrange, CinemaAmber, CinemaOrange)),
                                    CircleShape
                                )
                        ) {
                            SubcomposeAsyncImage(
                                model = if (profileId != null) {
                                    ImageRequest.Builder(context)
                                        .data("${BuildConfig.BASE_URL}images/users/$profileId?t=$photoTs")
                                        .memoryCachePolicy(CachePolicy.DISABLED)
                                        .diskCachePolicy(CachePolicy.DISABLED)
                                        .crossfade(true)
                                        .build()
                                } else null,
                                contentDescription = "Foto Profil",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            ) {
                                when (painter.state) {
                                    is AsyncImagePainter.State.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                                        CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp, color = CinemaOrange)
                                    }
                                    is AsyncImagePainter.State.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                                        Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp), tint = CinemaOrange.copy(0.5f))
                                    }
                                    else -> SubcomposeAsyncImageContent()
                                }
                            }
                        }
                    }

                    // Camera icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(CinemaOrange, CinemaOrangeDeep)))
                            .border(2.dp, Color(0xFF0D0800), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    if (uiState.profilePhoto is MovieActionUIState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(108.dp), strokeWidth = 3.dp, color = CinemaOrange)
                    }
                }

                // Name + username
                if (profile != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(profile.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(CinemaOrange.copy(0.12f))
                                .border(1.dp, CinemaOrange.copy(0.3f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("@${profile.username}", fontSize = 13.sp, color = CinemaAmber, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    CircularProgressIndicator(color = CinemaOrange)
                }

                // ── About card ─────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFF2A1500), Color(0xFF1A0A00))))
                        .border(1.dp, CinemaOrange.copy(0.2f), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Info, null, tint = CinemaAmber, modifier = Modifier.size(16.dp))
                                Text("Tentang", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold), color = CinemaAmber)
                            }
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CinemaOrange.copy(0.15f))
                                    .border(1.dp, CinemaOrange.copy(0.3f), RoundedCornerShape(8.dp))
                                    .clickable { movieViewModel.resetProfileAboutState(); showAboutSheet = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Edit, null, tint = CinemaOrange, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            profile?.about?.takeIf { it.isNotBlank() } ?: "Belum ada info tentang kamu.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (profile?.about.isNullOrBlank()) Color.White.copy(0.3f) else Color.White.copy(0.75f),
                            lineHeight = 20.sp
                        )
                    }
                }

                // ── Divider ────────────────────────────────────────────────────
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CinemaOrange.copy(0.12f)))

                // ── Action buttons ─────────────────────────────────────────────
                ProfileActionButton(
                    icon = Icons.Default.Edit,
                    label = "Edit Profil (Nama & Username)",
                    onClick = { movieViewModel.resetProfileUpdateState(); showEditSheet = true }
                )
                ProfileActionButton(
                    icon = Icons.Default.Lock,
                    label = "Ubah Kata Sandi",
                    onClick = { movieViewModel.resetProfilePasswordState(); showPwSheet = true }
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CinemaOrange.copy(0.12f)))

                // ── Logout ─────────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFF5C0000))))
                        .border(1.dp, Color(0xFFFF4444).copy(0.4f), RoundedCornerShape(14.dp))
                        .clickable { showLogoutDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Logout, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("Keluar dari Akun", fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
        }

        // FIX: BottomNav at the bottom - no overlap
        BottomNavComponent(navController)
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (showPhotoDialog) {
        CinemaDialog(
            title = "Ganti Foto Profil",
            message = "Foto akan dikompres sebelum diunggah. Lanjutkan?",
            confirmText = "Ya, Ganti",
            onConfirm = {
                pendingUri?.let {
                    movieViewModel.updatePhoto(authToken, ImageCompressHelper.uriToCompressedMultipart(context, it, "file"))
                }
                showPhotoDialog = false
            },
            onDismiss = { showPhotoDialog = false; pendingUri = null }
        )
    }

    if (showLogoutDialog) {
        CinemaDialog(
            title = "Keluar",
            message = "Yakin ingin keluar dari akun ini?",
            confirmText = "Ya, Keluar",
            isDestructive = true,
            onConfirm = { logoutTriggered = true; authViewModel.logout(authToken); showLogoutDialog = false },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showEditSheet) EditProfileSheet(
        profile?.name ?: "", profile?.username ?: "",
        onDismiss = { showEditSheet = false; movieViewModel.resetProfileUpdateState() },
        onSave = { n, u -> movieViewModel.updateProfile(authToken, n, u) },
        state = uiState.profileUpdate,
        onSuccess = { showEditSheet = false; movieViewModel.getProfile(authToken); movieViewModel.resetProfileUpdateState() }
    )
    if (showPwSheet) ChangePasswordSheet(
        onDismiss = { showPwSheet = false; movieViewModel.resetProfilePasswordState() },
        onSave = { o, n -> movieViewModel.updatePassword(authToken, o, n) },
        state = uiState.profilePassword,
        onSuccess = { showPwSheet = false; movieViewModel.resetProfilePasswordState() }
    )
    if (showAboutSheet) EditAboutSheet(
        profile?.about ?: "",
        onDismiss = { showAboutSheet = false; movieViewModel.resetProfileAboutState() },
        onSave = { movieViewModel.updateAbout(authToken, it) },
        state = uiState.profileAbout,
        onSuccess = { showAboutSheet = false; movieViewModel.getProfile(authToken); movieViewModel.resetProfileAboutState() }
    )
}

// ── Reusable Profile Action Button ────────────────────────────────────────────

@Composable
private fun ProfileActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF2A1500), Color(0xFF1A0A00))))
            .border(1.dp, CinemaOrange.copy(0.25f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(CinemaOrange.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = CinemaOrange, modifier = Modifier.size(16.dp))
            }
            Text(label, color = CinemaAmber, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = CinemaOrange.copy(0.5f), modifier = Modifier.size(18.dp))
        }
    }
}

// ── Cinema Dialog ─────────────────────────────────────────────────────────────

@Composable
private fun CinemaDialog(
    title: String,
    message: String,
    confirmText: String,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2A1500),
        shape = RoundedCornerShape(20.dp),
        title = { Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold) },
        text = { Text(message, color = Color.White.copy(0.7f)) },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            if (isDestructive) listOf(Color(0xFFCC0000), Color(0xFF880000))
                            else listOf(CinemaOrange, CinemaOrangeDeep)
                        )
                    )
                    .clickable(onClick = onConfirm)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(confirmText, color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = CinemaAmber) }
        }
    )
}

// ── Bottom Sheets ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(name: String, username: String, onDismiss: () -> Unit, onSave: (String, String) -> Unit, state: MovieActionUIState, onSuccess: () -> Unit) {
    var n by remember { mutableStateOf(name) }
    var u by remember { mutableStateOf(username) }
    LaunchedEffect(state) { if (state is MovieActionUIState.Success) onSuccess() }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFF2A1500), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Edit Profil", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
            cinemaField(n, { n = it }, "Nama")
            cinemaField(u, { u = it }, "Username")
            CinemaButton(label = "Simpan", loading = state is MovieActionUIState.Loading, onClick = { if (n.isNotBlank() && u.isNotBlank()) onSave(n, u) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordSheet(onDismiss: () -> Unit, onSave: (String, String) -> Unit, state: MovieActionUIState, onSuccess: () -> Unit) {
    var oldPw   by remember { mutableStateOf("") }
    var newPw   by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var err     by remember { mutableStateOf("") }
    LaunchedEffect(state) { if (state is MovieActionUIState.Success) onSuccess() }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFF2A1500), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Ubah Kata Sandi", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
            cinemaField(oldPw, { oldPw = it }, "Sandi Lama", isPassword = true)
            cinemaField(newPw, { newPw = it }, "Sandi Baru", isPassword = true)
            cinemaField(confirm, { confirm = it }, "Konfirmasi Sandi Baru", isPassword = true, isError = err.isNotEmpty())
            if (err.isNotEmpty()) Text(err, color = Color(0xFFFF5555), style = MaterialTheme.typography.bodySmall)
            CinemaButton(
                label = "Simpan",
                loading = state is MovieActionUIState.Loading,
                onClick = { if (newPw != confirm) { err = "Konfirmasi tidak cocok"; return@CinemaButton }; err = ""; onSave(oldPw, newPw) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAboutSheet(about: String, onDismiss: () -> Unit, onSave: (String) -> Unit, state: MovieActionUIState, onSuccess: () -> Unit) {
    var a by remember { mutableStateOf(about) }
    LaunchedEffect(state) { if (state is MovieActionUIState.Success) onSuccess() }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFF2A1500), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Edit Tentang", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
            cinemaField(a, { a = it }, "Tentang kamu", minLines = 4)
            CinemaButton(label = "Simpan", loading = state is MovieActionUIState.Loading, onClick = { onSave(a) })
        }
    }
}

@Composable
private fun cinemaField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    isError: Boolean = false,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        isError = isError,
        minLines = minLines,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White.copy(0.8f),
            focusedBorderColor = CinemaOrange,
            unfocusedBorderColor = Color.White.copy(0.15f),
            cursorColor = CinemaAmber,
            focusedLabelColor = CinemaOrange,
            unfocusedLabelColor = Color.White.copy(0.4f),
            focusedContainerColor = Color.White.copy(0.04f),
            unfocusedContainerColor = Color.Transparent
        )
    )
}

@Composable
private fun CinemaButton(label: String, loading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (!loading) Brush.horizontalGradient(listOf(CinemaOrange, CinemaOrangeDeep))
                else Brush.horizontalGradient(listOf(Color(0xFF5A3000), Color(0xFF5A3000)))
            )
            .clickable(enabled = !loading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
        else Text(label, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 15.sp)
    }
}