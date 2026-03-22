package org.delcom.pam_ifs23050_proyek1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import org.delcom.pam_ifs23050_proyek1.helper.ToolsHelper
import org.delcom.pam_ifs23050_proyek1.network.data.ResponseMovieData
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaAmber
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaOrange

@Composable
fun MovieItemUI(
    movie: ResponseMovieData,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val status = movie.watchStatus

    val coverUrl = remember(movie.cover, movie.updatedAt) {
        ToolsHelper.getMovieImageUrl(movie.cover, movie.updatedAt)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2A1500), Color(0xFF1E0F00))
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(CinemaOrange.copy(0.3f), Color.Transparent, CinemaOrange.copy(0.1f))
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top
        ) {
            // ── Poster ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(108.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(Color(0xFF3D1800)),
                contentAlignment = Alignment.Center
            ) {
                if (coverUrl != null) {
                    SubcomposeAsyncImage(
                        model = coverUrl,
                        contentDescription = movie.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    ) {
                        when (painter.state) {
                            is AsyncImagePainter.State.Loading -> {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = CinemaOrange)
                                }
                            }
                            is AsyncImagePainter.State.Error -> PlaceholderPoster()
                            else -> SubcomposeAsyncImageContent()
                        }
                    }
                } else {
                    PlaceholderPoster()
                }

                // Orange tint overlay on bottom of poster
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, CinemaOrange.copy(0.25f))
                            )
                        )
                )
            }

            // ── Info ──────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 0.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )

                if (movie.releaseYear != null) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = movie.releaseYear!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaAmber,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (movie.cleanDescription.isNotBlank()) {
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = movie.cleanDescription,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White.copy(0.5f)
                    )
                }

                Spacer(Modifier.height(8.dp))
                WatchStatusBadge(status = status)
            }

            // ── Delete ────────────────────────────────────────────────────────
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.padding(top = 4.dp, end = 4.dp).size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = Color(0xFFFF6B6B).copy(0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF2A1500),
            shape = RoundedCornerShape(20.dp),
            title = { Text("Hapus Film", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Yakin ingin menghapus \"${movie.title}\" dari watchlist?", color = Color.White.copy(0.7f)) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Hapus", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = CinemaAmber)
                }
            }
        )
    }
}

@Composable
fun PlaceholderPoster() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            Icons.Default.Movie,
            contentDescription = null,
            tint = CinemaOrange.copy(alpha = 0.4f),
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text("No Poster", fontSize = 8.sp, color = CinemaOrange.copy(alpha = 0.3f))
    }
}