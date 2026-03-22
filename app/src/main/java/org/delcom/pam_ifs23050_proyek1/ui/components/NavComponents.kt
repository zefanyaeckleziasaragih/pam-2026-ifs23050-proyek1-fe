package org.delcom.pam_ifs23050_proyek1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaAmber
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaOrange

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val iconActive: ImageVector
) {
    object Home    : BottomNavItem("home",    "Home",      Icons.Outlined.Home,   Icons.Filled.Home)
    object Movies  : BottomNavItem("movies",  "Watchlist", Icons.Outlined.Movie,  Icons.Filled.Movie)
    object Profile : BottomNavItem("profile", "Profil",    Icons.Outlined.Person, Icons.Filled.Person)
}

@Composable
fun BottomNavComponent(navController: NavHostController) {
    val items = listOf(BottomNavItem.Home, BottomNavItem.Movies, BottomNavItem.Profile)
    val currentRoute = navController.currentDestination?.route

    // SOLID background — no transparency so icons always visible
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1C0E00))
    ) {
        // Top orange accent line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            CinemaOrange.copy(0.9f),
                            CinemaAmber,
                            CinemaOrange.copy(0.9f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Nav items row — fixed 64dp height, no NavigationBar widget (it adds unwanted tonal elevation)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val selected = currentRoute?.startsWith(screen.route) == true
                BottomNavItemView(
                    item = screen,
                    selected = selected,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BottomNavItemView(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon box
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .then(
                    if (selected) Modifier
                        .background(CinemaOrange.copy(alpha = 0.22f))
                        .border(1.dp, CinemaOrange.copy(0.55f), RoundedCornerShape(12.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = if (selected) item.iconActive else item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(24.dp),
                    tint = if (selected) CinemaOrange else Color(0xFF8A6A4A)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.title,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (selected) CinemaAmber else Color(0xFF8A6A4A)
        )
    }
}

// ── Top App Bar ────────────────────────────────────────────────────────────────

data class TopBarMenuItem(
    val text: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchListTopBar(
    title: String,
    navController: NavHostController,
    showBackButton: Boolean = true,
    showMenu: Boolean = false,
    menuItems: List<TopBarMenuItem> = emptyList(),
    onBackClick: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A0A00))
    ) {
        // Bottom accent line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            CinemaOrange.copy(0.7f),
                            CinemaAmber,
                            CinemaOrange.copy(0.7f),
                            Color.Transparent
                        )
                    )
                )
        )

        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showBackButton) {
                        IconButton(
                            onClick = { onBackClick?.invoke() ?: navController.popBackStack() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CinemaOrange.copy(0.15f))
                                .border(1.dp, CinemaOrange.copy(0.3f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                Icons.Default.ArrowBack, "Back",
                                tint = CinemaAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.3.sp
                        ),
                        color = Color.White,
                        maxLines = 1
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            actions = {
                if (showMenu && menuItems.isNotEmpty()) {
                    Box(Modifier.padding(end = 8.dp)) {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, "Menu", tint = CinemaAmber)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color(0xFF2A1500))
                        ) {
                            menuItems.forEachIndexed { i, item ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                item.icon, null,
                                                modifier = Modifier.size(18.dp),
                                                tint = if (item.isDestructive) Color(0xFFFF5555) else CinemaAmber
                                            )
                                            Text(
                                                item.text,
                                                color = if (item.isDestructive) Color(0xFFFF5555) else Color.White,
                                                fontWeight = if (item.isDestructive) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = { expanded = false; item.onClick() }
                                )
                                if (i == menuItems.size - 2 && menuItems.last().isDestructive) {
                                    HorizontalDivider(color = CinemaOrange.copy(0.2f))
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

// ── Snackbar ───────────────────────────────────────────────────────────────────

@Composable
fun WatchListSnackbar(snackbarData: SnackbarData, onDismiss: () -> Unit) {
    val raw     = snackbarData.visuals.message
    val parts   = raw.split("|", limit = 2)
    val type    = parts.getOrNull(0) ?: "info"
    val message = parts.getOrNull(1) ?: raw

    val (icon, iconColor, bgColor) = when (type) {
        "error"   -> Triple(Icons.Default.Error,       Color(0xFFFF5555), Color(0xFF3D0D0D))
        "success" -> Triple(Icons.Default.CheckCircle, Color(0xFF4CAF50), Color(0xFF0D2A0D))
        "warning" -> Triple(Icons.Default.Warning,     CinemaAmber,       Color(0xFF2A1800))
        else      -> Triple(Icons.Default.Info,        CinemaOrange,      Color(0xFF2A1500))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, iconColor.copy(0.4f), RoundedCornerShape(14.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(message, color = Color.White, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.White.copy(0.5f), modifier = Modifier.size(14.dp))
            }
        }
    }
}