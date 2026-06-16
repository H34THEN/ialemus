package com.heathen.ialemus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.util.formatDuration

@Composable
fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    index: Int? = null,
) {
    val tokens = LocalIalemusTokens.current
    val borderColor = if (isPlaying) tokens.accentActive else tokens.hudBorderColor.copy(alpha = 0.3f)
    val backgroundColor = if (isPlaying) {
        tokens.panelOverlay.copy(alpha = 0.95f)
    } else {
        tokens.surfaceDeep.copy(alpha = 0.55f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, MaterialTheme.shapes.small)
            .border(1.dp, borderColor, MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (index != null) {
            Text(
                text = String.format("%03d", index + 1),
                style = MaterialTheme.typography.labelSmall,
                color = if (isPlaying) tokens.accentActive else tokens.textMuted,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (isPlaying) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = "Playing",
                        tint = tokens.accentActive,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Text(
                        text = track.displayTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isPlaying) tokens.glowColor else tokens.textPrimary,
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = track.displayArtist,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${track.displayAlbum} · ${formatDuration(track.durationMs)}",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.textMuted.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            HudStatusChip(
                label = track.sourceChipLabel,
                highlighted = isPlaying,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (onFavoriteClick != null) {
            IconButton(onClick = onFavoriteClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) tokens.warningColor else tokens.textMuted,
                )
            }
        }
        IconButton(onClick = { /* TODO(MVP 1B) */ }, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More",
                tint = tokens.textMuted,
            )
        }
    }
}
