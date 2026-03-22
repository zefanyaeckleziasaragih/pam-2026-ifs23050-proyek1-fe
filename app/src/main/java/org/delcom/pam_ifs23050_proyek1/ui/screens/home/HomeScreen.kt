package org.delcom.pam_ifs23050_proyek1.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.delcom.pam_ifs23050_proyek1.helper.RouteHelper
import org.delcom.pam_ifs23050_proyek1.network.data.ResponseMovieData
import org.delcom.pam_ifs23050_proyek1.network.data.WatchStatus
import org.delcom.pam_ifs23050_proyek1.ui.components.BottomNavComponent
import org.delcom.pam_ifs23050_proyek1.ui.components.MovieItemUI
import org.delcom.pam_ifs23050_proyek1.ui.components.WatchListTopBar
import org.delcom.pam_ifs23050_proyek1.ui.components.WatchStatusBadge
import org.delcom.pam_ifs23050_proyek1.ui.theme.*
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.*

@Composable
fun HomeScreen(
    navController: NavHostController,
    authToken: String,
    movieViewModel: MovieViewModel
) {
    val isAuth = authToken.isNotBlank()
    val uiState   by movieViewModel.uiState.collectAsState()
    val homeState by movieViewModel.homeMoviesState.collectAsState()

    LaunchedEffect(authToken) { if (isAuth) movieViewModel.getStats(authToken) }
    val stats = (uiState.stats as? StatsUIState.Success)?.data
    val statsLoading = uiState.stats is StatsUIState.Loading

    var nextPage    by remember { mutableStateOf(1) }
    var allMovies   by remember { mutableStateOf<List<ResponseMovieData>>(emptyList()) }
    var hasNextPage by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }
    var firstLoad   by remember { mutableStateOf(true) }
    val loadedIds   = remember { mutableSetOf<String>() }
    val listState   = rememberLazyListState()

    fun load(page: Int) { if (isAuth) movieViewModel.getHomeMovies(authToken, page, 10) }
    fun reset() {
        nextPage = 1; allMovies = emptyList(); loadedIds.clear()
        hasNextPage = false; loadingMore = false; firstLoad = true; load(1)
    }

    LaunchedEffect(authToken) { if (isAuth) reset() }
    LaunchedEffect(uiState.movieDelete) {
        if (uiState.movieDelete is MovieActionUIState.Success) {
            movieViewModel.resetMovieDeleteState()
            if (isAuth) { reset(); movieViewModel.getStats(authToken) }
        }
    }
    LaunchedEffect(homeState) {
        when (val s = homeState) {
            is HomeMoviesUIState.Success -> {
                val newItems = s.data.filter { it.id !in loadedIds }
                loadedIds.addAll(newItems.map { it.id })
                allMovies = if (firstLoad) newItems else allMovies + newItems
                firstLoad = false
                if (newItems.isNotEmpty() || s.pagination.hasNextPage) nextPage = s.pagination.currentPage + 1
                hasNextPage = s.pagination.hasNextPage; loadingMore = false
            }
            is HomeMoviesUIState.Error -> { loadingMore = false; firstLoad = false }
            else -> {}
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { it.visibleItemsInfo.lastOrNull()?.index?.let { i -> i >= it.totalItemsCount - 3 } ?: false }
            .distinctUntilChanged().filter { it }
            .collect { if (!loadingMore && hasNextPage && isAuth) { loadingMore = true; load(nextPage) } }
    }

    // FIX: Wrap in Column with BottomNavComponent OUTSIDE the scrollable content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0800))
    ) {
        WatchListTopBar(title = "WatchList", showBackButton = false, navController = navController)

        // FIX: Use weight(1f) so content doesn't overlap with bottom nav
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp) // Extra padding so FAB doesn't cover last item
            ) {
                // ── Hero Banner ───────────────────────────────────────────────
                item(key = "hero") {
                    HeroBanner(
                        isAuth = isAuth,
                        onLoginClick = { navController.navigate(RouteHelper.LOGIN) }
                    )
                }

                if (!isAuth) return@LazyColumn

                // ── Stats Section ─────────────────────────────────────────────
                item(key = "stats") {
                    StatsSection(stats = stats, loading = statsLoading)
                }

                // ── Status Legend ─────────────────────────────────────────────
                item(key = "legend") {
                    StatusLegendSection()
                }

                // ── Recent Films header ────────────────────────────────────────
                item(key = "recent_header") {
                    RecentHeader()
                }

                // ── Film list ─────────────────────────────────────────────────
                if (firstLoad && homeState is HomeMoviesUIState.Loading) {
                    item(key = "loading") {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = CinemaOrange, strokeWidth = 2.dp)
                        }
                    }
                } else if (!firstLoad && allMovies.isEmpty()) {
                    item(key = "empty") { EmptyState() }
                } else {
                    itemsIndexed(allMovies, key = { _, m -> m.id }) { _, movie ->
                        MovieItemUI(
                            movie = movie,
                            onClick = { navController.navigate(RouteHelper.movieDetail(movie.id)) },
                            onDelete = { movieViewModel.deleteMovie(authToken, movie.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }
                    if (loadingMore) {
                        item(key = "loading_more") {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(24.dp), color = CinemaOrange, strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }

            // FAB positioned over the list
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(CinemaOrange, CinemaOrangeDeep)))
                    .border(1.5.dp, CinemaAmber.copy(0.6f), RoundedCornerShape(18.dp))
                    .clickable {
                        navController.navigate(if (isAuth) RouteHelper.MOVIE_ADD else RouteHelper.LOGIN)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }

        // FIX: BottomNav is OUTSIDE the scrollable box - no overlap
        BottomNavComponent(navController)
    }
}

// ── Hero Banner ────────────────────────────────────────────────────────────────

@Composable
private fun HeroBanner(isAuth: Boolean, onLoginClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2A1200), Color(0xFF1A0A00), Color(0xFF0D0800))
                )
            )
    ) {
        // Decorative film perforations top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 10.dp, start = 6.dp, end = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(14) {
                Box(
                    Modifier
                        .size(width = 12.dp, height = 8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CinemaOrange.copy(0.25f))
                )
            }
        }

        // Glow effect
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.Center)
                .offset(y = (-10).dp)
                .background(Brush.radialGradient(listOf(CinemaOrange.copy(0.18f), Color.Transparent)))
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CinemaOrange.copy(0.15f))
                    .border(1.dp, CinemaOrange.copy(0.4f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Movie, null, tint = CinemaOrange, modifier = Modifier.size(28.dp))
            }
            Text(
                "Catatan Filmku",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White
            )
            Text(
                "Lacak semua film favoritmu",
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.3.sp),
                color = CinemaAmber.copy(0.7f)
            )
        }

        // Film perforations bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp, start = 6.dp, end = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(14) {
                Box(
                    Modifier
                        .size(width = 12.dp, height = 8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CinemaOrange.copy(0.25f))
                )
            }
        }

        if (!isAuth) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(CinemaOrange, CinemaOrangeDeep)))
                    .clickable(onClick = onLoginClick)
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text("Masuk / Daftar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// ── Stats Section ─────────────────────────────────────────────────────────────

@Composable
private fun StatsSection(
    stats: org.delcom.pam_ifs23050_proyek1.network.data.ResponseStatsData?,
    loading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.size(3.dp, 18.dp).clip(RoundedCornerShape(2.dp)).background(CinemaOrange))
            Text(
                "Statistik",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White
            )
        }

        // 3 stat cards in a row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label = "Total Film",
                value = stats?.total?.toString() ?: "–",
                icon = Icons.Default.VideoLibrary,
                accent = CinemaOrange,
                loading = loading,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Ditonton",
                value = stats?.done?.toString() ?: "–",
                icon = Icons.Default.CheckCircle,
                accent = Color(0xFF2E7D32),
                loading = loading,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Belum",
                value = stats?.pending?.toString() ?: "–",
                icon = Icons.Default.Schedule,
                accent = Color(0xFF7B1FA2),
                loading = loading,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    loading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2A1400).copy(0.95f), Color(0xFF1A0A00))
                )
            )
            .border(1.dp, accent.copy(0.3f), RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon with accent background
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accent.copy(0.15f))
                    .border(1.dp, accent.copy(0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            }
            if (loading) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = accent)
            } else {
                Text(
                    value,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.45f), textAlign = TextAlign.Center)
        }
    }
}

// ── Status Legend ─────────────────────────────────────────────────────────────

@Composable
private fun StatusLegendSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.size(3.dp, 18.dp).clip(RoundedCornerShape(2.dp)).background(CinemaOrange))
            Text(
                "Status Tontonan",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White
            )
        }

        // 3 status cards horizontal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WatchStatus.entries.forEach { status ->
                StatusChipCard(status = status, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatusChipCard(status: WatchStatus, modifier: Modifier = Modifier) {
    val dotColor = Color(status.dotColorHex)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(dotColor.copy(0.18f), dotColor.copy(0.05f))))
            .border(1.dp, dotColor.copy(0.4f), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(dotColor, CircleShape)
            )
            Text(
                status.label,
                color = dotColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

// ── Recent Header ─────────────────────────────────────────────────────────────

@Composable
private fun RecentHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.size(3.dp, 20.dp).clip(RoundedCornerShape(2.dp)).background(CinemaOrange))
        Text(
            "Film Terbaru",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            ),
            color = Color.White
        )
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1E0F00))
            .border(1.dp, CinemaOrange.copy(0.15f), RoundedCornerShape(18.dp))
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Movie, null, tint = CinemaOrange.copy(0.3f), modifier = Modifier.size(48.dp))
            Text("Belum ada film di sini", color = Color.White.copy(0.5f), fontWeight = FontWeight.SemiBold)
            Text("Tap + untuk menambahkan film pertamamu!", color = Color.White.copy(0.3f), fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}