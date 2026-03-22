package org.delcom.pam_ifs23050_proyek1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.delcom.pam_ifs23050_proyek1.network.data.WatchStatus
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaAmber
import org.delcom.pam_ifs23050_proyek1.ui.theme.CinemaOrange

@Composable
fun WatchStatusBadge(status: WatchStatus, modifier: Modifier = Modifier) {
    val dotColor = Color(status.dotColorHex)
    val bgColor  = Color(status.bgColorHex).copy(0.15f)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(0.5.dp, dotColor.copy(0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(7.dp).background(dotColor, CircleShape))
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = status.label,
            color = dotColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun WatchStatusSelector(
    selectedStatus: WatchStatus,
    onStatusSelected: (WatchStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = WatchStatus.values()
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { status ->
            val isSelected = selectedStatus == status
            val dotColor = Color(status.dotColorHex)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) Brush.verticalGradient(
                            listOf(dotColor.copy(0.2f), dotColor.copy(0.05f))
                        ) else Brush.verticalGradient(
                            listOf(Color(0xFF2A1500), Color(0xFF1E0F00))
                        )
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 0.5.dp,
                        color = if (isSelected) dotColor.copy(0.8f) else Color.White.copy(0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onStatusSelected(status) }
                    .padding(vertical = 12.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isSelected) dotColor else Color.White.copy(0.25f),
                                CircleShape
                            )
                    )
                    Text(
                        text = status.label,
                        color = if (isSelected) dotColor else Color.White.copy(0.4f),
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}