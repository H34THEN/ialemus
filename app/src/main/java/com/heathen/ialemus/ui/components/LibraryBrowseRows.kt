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
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.heathen.ialemus.core.model.AlbumSummary
import com.heathen.ialemus.core.model.ArtistSummary
import com.heathen.ialemus.core.model.FolderSummary
import com.heathen.ialemus.ui.theme.LocalIalemusTokens
import com.heathen.ialemus.ui.util.formatDuration

@Composable
fun ArtistBrowseRow(
    summary: ArtistSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = BrowseSummaryRow(
    title = summary.displayName,
    subtitle = "${summary.albumCount} albums · ${summary.trackCount} tracks",
    icon = Icons.Filled.Person,
    onClick = onClick,
    modifier = modifier,
)

@Composable
fun AlbumBrowseRow(
    summary: AlbumSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = BrowseSummaryRow(
    title = summary.displayAlbum,
    subtitle = "${summary.displayArtist} · ${summary.trackCount} tracks · ${formatDuration(summary.totalDurationMs)}",
    icon = Icons.Filled.Album,
    onClick = onClick,
    modifier = modifier,
)

@Composable
fun FolderBrowseRow(
    summary: FolderSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = BrowseSummaryRow(
    title = summary.displayName,
    subtitle = "${summary.sourceLabel} · ${summary.trackCount} tracks",
    icon = Icons.Filled.Folder,
    onClick = onClick,
    modifier = modifier,
    meta = summary.safePathLabel,
)

@Composable
fun AudiobookBrowseRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = BrowseSummaryRow(
    title = title,
    subtitle = subtitle,
    icon = Icons.Filled.Mic,
    onClick = onClick,
    modifier = modifier,
    accentWarning = true,
)

@Composable
private fun BrowseSummaryRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    meta: String? = null,
    accentWarning: Boolean = false,
) {
    val tokens = LocalIalemusTokens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(tokens.surfaceDeep.copy(alpha = 0.55f), MaterialTheme.shapes.medium)
            .border(1.dp, tokens.hudBorderColor.copy(alpha = 0.35f), MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (accentWarning) tokens.warningColor else tokens.accentActive,
            modifier = Modifier.size(28.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = tokens.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (meta != null) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textMuted.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
