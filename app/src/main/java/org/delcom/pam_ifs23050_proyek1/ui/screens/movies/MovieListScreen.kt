package org.delcom.pam_ifs23050_proyek1.ui.screens.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.delcom.pam_ifs23050_proyek1.network.data.ResponseMovieData
import org.delcom.pam_ifs23050_proyek1.network.data.WatchStatus
import org.delcom.pam_ifs23050_proyek1.ui.components.*
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaAmber
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaOrange
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaOrangeDeep
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.MovieActionUIState
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.MovieViewModel
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.MoviesUIState

private const val PER_PAGE = 10

@Composable
fun MovieListScreen(
    navController: NavHostController,
    authToken: String,
    movieViewModel: MovieViewModel,
) {
    if (authToken.isBlank()) {
        LaunchedEffect(Unit) { navController.navigate("auth/login") { popUpTo(0) { inclusive = true } } }
        return
    }

    val uiState by movieViewModel.uiState.collectAsState()
    val navBackStack by navController.currentBackStackEntryAsState()

    var selectedStatus by remember { mutableStateOf<WatchStatus?>(null) }
    var nextPageToLoad by remember { mutableStateOf(1) }
    var allMovies by remember { mutableStateOf<List<ResponseMovieData>>(emptyList()) }
    var hasNextPage by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var isFirstLoad by remember { mutableStateOf(true) }
    val loadedIds = remember { mutableSetOf<String>() }
    val listState = rememberLazyListState()

    fun loadPage(page: Int, urgency: String?) =
        movieViewModel.getAllMovies(authToken, page = page, perPage = PER_PAGE, urgency = urgency)

    fun resetAndLoad(urgency: String? = selectedStatus?.apiValue) {
        nextPageToLoad = 1; allMovies = emptyList(); loadedIds.clear()
        hasNextPage = false; isLoadingMore = false; isFirstLoad = true
        loadPage(1, urgency)
    }

    LaunchedEffect(authToken) { resetAndLoad() }

    LaunchedEffect(navBackStack) {
        val handle = navBackStack?.savedStateHandle
        if (handle?.get<Boolean>("movie_added") == true) {
            handle.remove<Boolean>("movie_added")
            resetAndLoad()
        }
    }

    LaunchedEffect(uiState.movieDelete) {
        if (uiState.movieDelete is MovieActionUIState.Success) {
            movieViewModel.resetMovieDeleteState()
            resetAndLoad()
        }
    }

    LaunchedEffect(uiState.movies) {
        when (val state = uiState.movies) {
            is MoviesUIState.Success -> {
                val newItems = state.data.filter { it.id !in loadedIds }
                loadedIds.addAll(newItems.map { it.id })
                allMovies = if (isFirstLoad) { isFirstLoad = false; newItems } else allMovies + newItems
                nextPageToLoad = state.pagination.currentPage + 1
                hasNextPage = state.pagination.hasNextPage
                isLoadingMore = false
            }
            is MoviesUIState.Error -> { isLoadingMore = false; isFirstLoad = false }
            is MoviesUIState.Loading -> {}
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { (it.visibleItemsInfo.lastOrNull()?.index ?: 0) to it.totalItemsCount }
            .distinctUntilChanged()
            .filter { (last, total) -> total > 0 && last >= total - 3 }
            .collect {
                if (!isLoadingMore && hasNextPage) {
                    isLoadingMore = true
                    loadPage(nextPageToLoad, selectedStatus?.apiValue)
                }
            }
    }

    // FIX: Column layout - TopBar + Content(weight 1) + BottomNav
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0800))
    ) {
        WatchListTopBar(title = "Watchlist Saya", navController = navController, showBackButton = false)

        // FIX: weight(1f) ensures content fills space between topbar and bottomnav
        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Filter chips ───────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterPill(
                        label = "Semua",
                        selected = selectedStatus == null,
                        accent = CinemaOrange,
                        onClick = { selectedStatus = null; resetAndLoad(null) }
                    )
                    WatchStatus.entries.forEach { status ->
                        FilterPill(
                            label = status.label,
                            selected = selectedStatus == status,
                            accent = Color(status.dotColorHex),
                            onClick = {
                                selectedStatus = if (selectedStatus == status) null else status
                                resetAndLoad(selectedStatus?.apiValue)
                            }
                        )
                    }
                }

                // ── List or State ──────────────────────────────────────────────
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        isFirstLoad && uiState.movies is MoviesUIState.Loading -> {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                CircularProgressIndicator(color = CinemaOrange, strokeWidth = 2.dp)
                            }
                        }
                        !isFirstLoad && allMovies.isEmpty() -> {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(CinemaOrange.copy(0.1f))
                                            .border(1.dp, CinemaOrange.copy(0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Movie, null, tint = CinemaOrange.copy(0.5f), modifier = Modifier.size(40.dp))
                                    }
                                    Text("Belum ada film", color = Color.White.copy(0.6f), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    Text("Tap + untuk menambahkan film pertamamu!", color = Color.White.copy(0.3f), fontSize = 13.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                        allMovies.isNotEmpty() -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp, end = 16.dp,
                                    top = 4.dp, bottom = 88.dp // space for FAB
                                ),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(allMovies, key = { _, m -> m.id }) { _, movie ->
                                    MovieItemUI(
                                        movie = movie,
                                        onClick = { navController.navigate("movies/${movie.id}") },
                                        onDelete = { movieViewModel.deleteMovie(authToken, movie.id) }
                                    )
                                }
                                if (isLoadingMore) {
                                    item(key = "loading_more") {
                                        Box(Modifier.fillMaxWidth().padding(12.dp), Alignment.Center) {
                                            CircularProgressIndicator(Modifier.size(24.dp), color = CinemaOrange, strokeWidth = 2.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // FAB
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 20.dp, bottom = 20.dp)
                            .size(58.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.linearGradient(listOf(CinemaOrange, CinemaOrangeDeep)))
                            .border(1.5.dp, CinemaAmber.copy(0.6f), RoundedCornerShape(18.dp))
                            .clickable { navController.navigate("movies/add") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, "Tambah Film", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }

        // FIX: BottomNav is at the bottom of the Column - no overlap
        BottomNavComponent(navController = navController)
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) Brush.horizontalGradient(listOf(accent.copy(0.3f), accent.copy(0.15f)))
                else Brush.horizontalGradient(listOf(Color(0xFF2A1400), Color(0xFF1E0E00)))
            )
            .border(1.dp, if (selected) accent.copy(0.8f) else Color.White.copy(0.1f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) accent else Color.White.copy(0.45f),
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}