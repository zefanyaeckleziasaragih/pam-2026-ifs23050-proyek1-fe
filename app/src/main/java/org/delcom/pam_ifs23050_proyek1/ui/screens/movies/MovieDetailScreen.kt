package org.delcom.pam_ifs23050_proyek1.ui.screens.movies

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.CachePolicy
import coil.request.ImageRequest
import org.delcom.pam_ifs23050_proyek1.helper.ImageCompressHelper
import org.delcom.pam_ifs23050_proyek1.helper.RouteHelper
import org.delcom.pam_ifs23050_proyek1.helper.ToolsHelper
import org.delcom.pam_ifs23050_proyek1.network.data.ResponseMovieData
import org.delcom.pam_ifs23050_proyek1.ui.components.*
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaAmber
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaOrange
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaOrangeDeep
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.AuthUIState
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.AuthViewModel
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.MovieActionUIState
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.MovieUIState
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.MovieViewModel

@Composable
fun MovieDetailScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    movieViewModel: MovieViewModel,
    movieId: String
) {
    val uiState     by movieViewModel.uiState.collectAsState()
    val uiStateAuth by authViewModel.uiState.collectAsState()

    val authToken = remember(uiStateAuth.auth) {
        (uiStateAuth.auth as? AuthUIState.Success)?.data?.authToken
    }

    var isLoading        by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var coverTs          by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val movie = (uiState.movie as? MovieUIState.Success)?.data

    LaunchedEffect(Unit) {
        if (uiStateAuth.auth !is AuthUIState.Success) {
            navController.navigate(RouteHelper.LOGIN) { popUpTo(0) { inclusive = true } }
            return@LaunchedEffect
        }
        movieViewModel.resetMovieDetailState()
        val token = authToken ?: run { navController.popBackStack(); return@LaunchedEffect }
        movieViewModel.getMovieById(token, movieId)
    }

    LaunchedEffect(uiState.movie) {
        when (uiState.movie) {
            is MovieUIState.Success -> isLoading = false
            is MovieUIState.Error   -> { isLoading = false; navController.popBackStack() }
            is MovieUIState.Loading -> isLoading = true
        }
    }

    fun onDelete() { val token = authToken ?: return; isLoading = true; movieViewModel.deleteMovie(token, movieId) }

    LaunchedEffect(uiState.movieDelete) {
        when (val s = uiState.movieDelete) {
            is MovieActionUIState.Success -> { movieViewModel.resetMovieDeleteState(); navController.navigate(RouteHelper.MOVIES) { popUpTo(RouteHelper.MOVIES) { inclusive = true } } }
            is MovieActionUIState.Error -> { snackbarHost.showSnackbar("error|${s.message}"); isLoading = false }
            is MovieActionUIState.Loading -> isLoading = true
            else -> {}
        }
    }

    LaunchedEffect(uiState.movieChangeCover) {
        when (val s = uiState.movieChangeCover) {
            is MovieActionUIState.Success -> {
                snackbarHost.showSnackbar("success|Poster berhasil diperbarui")
                movieViewModel.resetMovieCoverState()
                coverTs = System.currentTimeMillis()
                authToken?.let { movieViewModel.getMovieById(it, movieId) }
                isLoading = false
            }
            is MovieActionUIState.Error -> { snackbarHost.showSnackbar("error|${s.message}"); movieViewModel.resetMovieCoverState(); isLoading = false }
            is MovieActionUIState.Loading -> isLoading = true
            else -> {}
        }
    }

    if (isLoading || movie == null) {
        Box(
            Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0D0800), Color(0xFF1A0F00)))),
            Alignment.Center
        ) { CircularProgressIndicator(color = CinemaOrange) }
        return
    }

    val menuItems = listOf(
        TopBarMenuItem("Edit Film", Icons.Default.Edit, { navController.navigate(RouteHelper.movieEdit(movie.id)) }),
        TopBarMenuItem("Hapus Film", Icons.Default.Delete, { showDeleteDialog = true }, isDestructive = true)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D0800), Color(0xFF1A0F00))))
    ) {
        WatchListTopBar(title = movie.title, navController = navController, showBackButton = true, showMenu = true, menuItems = menuItems)

        Box(modifier = Modifier.weight(1f)) {
            MovieDetailContent(
                movie = movie,
                coverTs = coverTs,
                onChangeCover = { uri, context ->
                    val token = authToken ?: return@MovieDetailContent
                    isLoading = true
                    val part = ImageCompressHelper.uriToCompressedMultipart(context, uri, "file")
                    movieViewModel.putMovieCover(token, movie.id, part)
                }
            )
        }

        BottomNavComponent(navController = navController)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF2A1500),
            shape = RoundedCornerShape(20.dp),
            title = { Text("Hapus Film", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Yakin ingin menghapus \"${movie.title}\" dari watchlist?", color = Color.White.copy(0.7f)) },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFF4444), Color(0xFFCC0000))))
                ) {
                    Button(
                        onClick = { showDeleteDialog = false; onDelete() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) { Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Batal", color = CinemaAmber) }
            }
        )
    }
}

@Composable
private fun MovieDetailContent(
    movie: ResponseMovieData,
    coverTs: Long,
    onChangeCover: (Uri, android.content.Context) -> Unit
) {
    var pendingUri       by remember { mutableStateOf<Uri?>(null) }
    var showCoverConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { pendingUri = it; showCoverConfirm = true }
    }

    val status = movie.watchStatus

    val coverUrl = remember(movie.cover, coverTs) {
        ToolsHelper.getMovieImageUrl(movie.cover, coverTs.toString())
    }
    val hasCover = coverUrl != null

    val coverImageRequest = remember(coverUrl) {
        if (coverUrl == null) null
        else ImageRequest.Builder(context)
            .data(coverUrl)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .crossfade(true)
            .build()
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        // ── Hero poster ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFF1E0A00))
                .clickable {
                    imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
        ) {
            when {
                pendingUri != null -> {
                    SubcomposeAsyncImage(model = pendingUri, contentDescription = movie.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) {
                        when (painter.state) {
                            is AsyncImagePainter.State.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = CinemaOrange) }
                            is AsyncImagePainter.State.Error -> LargePlaceholderPoster()
                            else -> SubcomposeAsyncImageContent()
                        }
                    }
                }
                hasCover -> key(coverTs) {
                    SubcomposeAsyncImage(model = coverImageRequest, contentDescription = movie.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) {
                        when (painter.state) {
                            is AsyncImagePainter.State.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = CinemaOrange) }
                            is AsyncImagePainter.State.Error -> LargePlaceholderPoster()
                            else -> SubcomposeAsyncImageContent()
                        }
                    }
                }
                else -> LargePlaceholderPoster()
            }

            // Orange gradient overlay
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp).align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xFF1A0F00).copy(0.95f))
                        )
                    )
            )

            // Camera button
            Box(
                modifier = Modifier.align(Alignment.BottomEnd).padding(14.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(CinemaOrange, CinemaOrangeDeep)))
                    .border(1.dp, CinemaAmber.copy(0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, "Ganti Poster", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        // ── Pending cover action bar ──────────────────────────────────────────
        if (pendingUri != null) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(CinemaOrange.copy(0.15f))
                    .border(1.dp, CinemaOrange.copy(0.3f), RoundedCornerShape(0.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Poster baru dipilih", style = MaterialTheme.typography.bodySmall, color = CinemaAmber)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { pendingUri = null }) { Text("Batal", color = Color.White.copy(0.6f)) }
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .background(Brush.horizontalGradient(listOf(CinemaOrange, CinemaOrangeDeep)))
                        ) {
                            Button(onClick = { showCoverConfirm = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                                Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ── Info section ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    movie.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                if (movie.releaseYear != null) {
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CinemaOrange.copy(0.2f))
                            .border(1.dp, CinemaOrange.copy(0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            movie.releaseYear!!,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = CinemaAmber,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            WatchStatusBadge(status = status)

            if (movie.cleanDescription.isNotBlank()) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CinemaOrange.copy(0.2f)))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Deskripsi", style = MaterialTheme.typography.labelMedium, color = CinemaAmber, fontWeight = FontWeight.Bold)
                    Text(movie.cleanDescription, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.7f), lineHeight = 22.sp)
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CinemaOrange.copy(0.2f)))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetaInfoItem("Ditambahkan", movie.createdAt.take(10), Modifier.weight(1f))
                MetaInfoItem("Terakhir diubah", movie.updatedAt.take(10), Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showCoverConfirm && pendingUri != null) {
        AlertDialog(
            onDismissRequest = { showCoverConfirm = false },
            containerColor = Color(0xFF2A1500),
            shape = RoundedCornerShape(20.dp),
            title = { Text("Ganti Poster?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Poster akan dikompres sebelum diunggah. Lanjutkan?", color = Color.White.copy(0.7f)) },
            confirmButton = {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        .background(Brush.horizontalGradient(listOf(CinemaOrange, CinemaOrangeDeep)))
                ) {
                    Button(
                        onClick = { showCoverConfirm = false; pendingUri?.let { onChangeCover(it, context) }; pendingUri = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) { Text("Ya, Simpan", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCoverConfirm = false; pendingUri = null }) { Text("Batal", color = CinemaAmber) }
            }
        )
    }
}

@Composable
private fun LargePlaceholderPoster() {
    Box(
        Modifier.fillMaxSize().background(Color(0xFF1E0A00)),
        Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Movie, null, tint = CinemaOrange.copy(0.3f), modifier = Modifier.size(64.dp))
            Text("Tap untuk menambahkan poster", style = MaterialTheme.typography.bodySmall, color = CinemaOrange.copy(0.4f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun MetaInfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2A1500))
            .border(1.dp, CinemaOrange.copy(0.15f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = CinemaAmber.copy(0.7f))
            Text(value, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}