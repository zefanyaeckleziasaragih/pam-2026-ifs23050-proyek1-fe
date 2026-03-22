package org.delcom.pam_ifs23050_proyek1.ui.screens.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.delcom.pam_ifs23050_proyek1.network.data.WatchStatus
import org.delcom.pam_ifs23050_proyek1.ui.components.WatchListSnackbar
import org.delcom.pam_ifs23050_proyek1.ui.components.WatchStatusSelector
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaAmber
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaOrange
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaOrangeDeep
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.MovieActionUIState
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.MovieUIState
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.MovieViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieEditScreen(
    authToken: String,
    movieId: String,
    movieViewModel: MovieViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by movieViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var title       by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var releaseYear by remember { mutableStateOf("") }
    var watchStatus by remember { mutableStateOf(WatchStatus.PLANNED) }
    var isLoading   by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (authToken.isNotBlank() && !initialized) {
            movieViewModel.getMovieById(authToken, movieId)
        }
    }

    LaunchedEffect(uiState.movie) {
        val state = uiState.movie
        if (!initialized && state is MovieUIState.Success) {
            val m = state.data
            title       = m.title
            description = m.cleanDescription
            releaseYear = m.releaseYear ?: ""
            watchStatus = m.watchStatus
            initialized = true
        }
    }

    LaunchedEffect(uiState.movieChange) {
        when (val s = uiState.movieChange) {
            is MovieActionUIState.Success -> {
                isLoading = false
                snackbarHostState.showSnackbar("success|Film berhasil diperbarui")
                authToken.let { movieViewModel.getMovieById(it, movieId) }
                onNavigateBack()
            }
            is MovieActionUIState.Error -> {
                isLoading = false
                snackbarHostState.showSnackbar("error|${s.message}")
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D0800), Color(0xFF1A0F00))))
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    WatchListSnackbar(data) { snackbarHostState.currentSnackbarData?.dismiss() }
                }
            },
            containerColor = Color.Transparent,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E0F00))
                ) {
                    TopAppBar(
                        title = { Text("Edit Film", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ArrowBack, "Kembali", tint = CinemaAmber)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            }
        ) { padding ->
            if (!initialized) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CinemaOrange)
                }
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                cinemaOutlinedTextField(value = title, onValueChange = { title = it }, label = "Judul Film *", placeholder = "Contoh: Interstellar", singleLine = true)
                cinemaOutlinedTextField(value = releaseYear, onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) releaseYear = it }, label = "Tahun Rilis (opsional)", placeholder = "Contoh: 2014", singleLine = true)
                cinemaOutlinedTextField(value = description, onValueChange = { description = it }, label = "Deskripsi / Catatan (opsional)", placeholder = "Ceritakan sedikit tentang film ini...", minLines = 3, maxLines = 6)

                Text("Status Tonton", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = CinemaAmber)
                WatchStatusSelector(selectedStatus = watchStatus, onStatusSelected = { watchStatus = it })

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (!isLoading && title.isNotBlank())
                                Brush.horizontalGradient(listOf(CinemaOrange, CinemaOrangeDeep))
                            else
                                Brush.horizontalGradient(listOf(Color(0xFF5A3000), Color(0xFF5A3000)))
                        )
                ) {
                    Button(
                        onClick = {
                            if (title.isBlank()) return@Button
                            isLoading = true
                            val finalDesc = buildString {
                                if (releaseYear.isNotBlank()) append("[$releaseYear] ")
                                append(description)
                            }.trim()
                            movieViewModel.putMovie(
                                authToken   = authToken,
                                movieId     = movieId,
                                title       = title.trim(),
                                description = finalDesc,
                                isDone      = watchStatus == WatchStatus.COMPLETED,
                                watchStatus = watchStatus.apiValue
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading && title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent)
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        else Text("Simpan Perubahan", fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }
    }
}