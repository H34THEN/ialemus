package com.heathen.ialemus.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heathen.ialemus.core.library.LibraryViewModel
import com.heathen.ialemus.core.model.LibraryBrowseMode
import com.heathen.ialemus.core.model.LibraryDetail
import com.heathen.ialemus.core.model.SignalBrowseMode
import com.heathen.ialemus.core.model.Track
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.ui.components.AlbumBrowseRow
import com.heathen.ialemus.ui.components.ArtistBrowseRow
import com.heathen.ialemus.ui.components.AudiobookBrowseRow
import com.heathen.ialemus.ui.components.EmptyLibraryState
import com.heathen.ialemus.ui.components.FolderBrowseRow
import com.heathen.ialemus.ui.components.HudButton
import com.heathen.ialemus.ui.components.HudPanel
import com.heathen.ialemus.ui.components.HudStatusChip
import com.heathen.ialemus.ui.components.PlaceholderCard
import com.heathen.ialemus.ui.components.TrackRow
import com.heathen.ialemus.ui.theme.LocalIalemusTokens

private val ListBottomPadding = PaddingValues(bottom = 12.dp)

@Composable
fun LibraryBrowserContent(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    browseMode: LibraryBrowseMode,
    onOpenDetail: (LibraryDetail) -> Unit,
    currentTrackId: String?,
    modifier: Modifier = Modifier,
) {
    when (browseMode) {
        LibraryBrowseMode.TRACKS -> TracksBrowsePane(
            libraryViewModel = libraryViewModel,
            playerViewModel = playerViewModel,
            currentTrackId = currentTrackId,
            modifier = modifier,
        )
        LibraryBrowseMode.ARTISTS -> ArtistsBrowsePane(
            libraryViewModel = libraryViewModel,
            onOpenDetail = onOpenDetail,
            modifier = modifier,
        )
        LibraryBrowseMode.ALBUMS -> AlbumsBrowsePane(
            libraryViewModel = libraryViewModel,
            onOpenDetail = onOpenDetail,
            modifier = modifier,
        )
        LibraryBrowseMode.GENRES -> GenresBrowsePane(modifier = modifier)
        LibraryBrowseMode.PLAYLISTS -> PlaylistsBrowsePane(modifier = modifier)
        LibraryBrowseMode.FOLDERS -> FoldersBrowsePane(
            libraryViewModel = libraryViewModel,
            onOpenDetail = onOpenDetail,
            modifier = modifier,
        )
        LibraryBrowseMode.AUDIOBOOKS -> AudiobooksBrowsePane(
            libraryViewModel = libraryViewModel,
            playerViewModel = playerViewModel,
            currentTrackId = currentTrackId,
            modifier = modifier,
        )
        LibraryBrowseMode.SIGNAL -> SignalBrowsePane(
            libraryViewModel = libraryViewModel,
            playerViewModel = playerViewModel,
            currentTrackId = currentTrackId,
            modifier = modifier,
        )
    }
}

@Composable
fun LibraryDetailContent(
    detail: LibraryDetail,
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    currentTrackId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (detail) {
        is LibraryDetail.Artist -> ArtistDetailPane(
            artist = detail.artist,
            libraryViewModel = libraryViewModel,
            playerViewModel = playerViewModel,
            currentTrackId = currentTrackId,
            onBack = onBack,
            modifier = modifier,
        )
        is LibraryDetail.Album -> AlbumDetailPane(
            album = detail.album,
            artist = detail.artist,
            libraryViewModel = libraryViewModel,
            playerViewModel = playerViewModel,
            currentTrackId = currentTrackId,
            onBack = onBack,
            modifier = modifier,
        )
        is LibraryDetail.Folder -> FolderDetailPane(
            librarySourceId = detail.librarySourceId,
            folderPath = detail.folderPath,
            displayName = detail.displayName,
            libraryViewModel = libraryViewModel,
            playerViewModel = playerViewModel,
            currentTrackId = currentTrackId,
            onBack = onBack,
            modifier = modifier,
        )
    }
}

@Composable
private fun TracksBrowsePane(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    currentTrackId: String?,
    modifier: Modifier = Modifier,
) {
    val tracks by libraryViewModel.tracks.collectAsStateWithLifecycle()
    TrackListPane(
        tracks = tracks,
        currentTrackId = currentTrackId,
        onTrackClick = { track -> playerViewModel.playTrack(tracks, track) },
        emptyTitle = "No local tracks",
        emptyBody = "Choose a music folder or run an explicit scan. Full-device scan is opt-in only.",
        modifier = modifier,
    )
}

@Composable
private fun ArtistsBrowsePane(
    libraryViewModel: LibraryViewModel,
    onOpenDetail: (LibraryDetail) -> Unit,
    modifier: Modifier = Modifier,
) {
    val artists by libraryViewModel.artistSummaries.collectAsStateWithLifecycle()
    if (artists.isEmpty()) {
        EmptyLibraryState(
            title = "No artists indexed",
            body = "Scan music sources to build artist summaries from track metadata.",
            sectionTag = "TRACK INDEX",
            modifier = modifier,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = ListBottomPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(artists, key = { it.artist }) { artist ->
            ArtistBrowseRow(
                summary = artist,
                onClick = { onOpenDetail(LibraryDetail.Artist(artist.artist)) },
            )
        }
    }
}

@Composable
private fun AlbumsBrowsePane(
    libraryViewModel: LibraryViewModel,
    onOpenDetail: (LibraryDetail) -> Unit,
    modifier: Modifier = Modifier,
) {
    val albums by libraryViewModel.albumSummaries.collectAsStateWithLifecycle()
    if (albums.isEmpty()) {
        EmptyLibraryState(
            title = "No albums indexed",
            body = "Scan music sources to build album summaries from track metadata.",
            sectionTag = "TRACK INDEX",
            modifier = modifier,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = ListBottomPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(albums, key = { "${it.album}|${it.artist}" }) { album ->
            AlbumBrowseRow(
                summary = album,
                onClick = { onOpenDetail(LibraryDetail.Album(album.album, album.artist)) },
            )
        }
    }
}

@Composable
private fun FoldersBrowsePane(
    libraryViewModel: LibraryViewModel,
    onOpenDetail: (LibraryDetail) -> Unit,
    modifier: Modifier = Modifier,
) {
    val folders by libraryViewModel.folderSummaries.collectAsStateWithLifecycle()
    if (folders.isEmpty()) {
        EmptyLibraryState(
            title = "No folder paths indexed",
            body = "Approve SAF music folders and scan to populate folder summaries.",
            sectionTag = "SOURCE SELECT",
            modifier = modifier,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = ListBottomPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(folders, key = { "${it.librarySourceId}|${it.folderPath}" }) { folder ->
            FolderBrowseRow(
                summary = folder,
                onClick = {
                    onOpenDetail(
                        LibraryDetail.Folder(
                            librarySourceId = folder.librarySourceId,
                            folderPath = folder.folderPath,
                            displayName = folder.displayName,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun GenresBrowsePane(modifier: Modifier = Modifier) {
    EmptyLibraryState(
        title = "Genre metadata not indexed yet",
        body = "Genre extraction from MediaStore and metadata retriever is planned. No fake genre data is shown.",
        sectionTag = "TRACK INDEX",
        modifier = modifier,
        actions = {
            HudStatusChip(label = "TODO: MediaStore genres", disabled = true)
            Text(
                text = "TODO: enrich tags via MediaMetadataRetriever during scan.",
                style = MaterialTheme.typography.bodySmall,
                color = LocalIalemusTokens.current.textMuted,
            )
        },
    )
}

@Composable
private fun PlaylistsBrowsePane(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PlaceholderCard(
            title = "Local playlists",
            body = "Room playlist schema scaffold — create/add tracks arrives in a future pass.",
            sectionTag = "PLAYLISTS",
        )
        PlaceholderCard(
            title = "M3U / M3U8 import",
            body = "Import/export playlist files — placeholder only.",
            sectionTag = "FUTURE MODULE",
        )
        PlaceholderCard(
            title = "NAS / spot-dl playlists",
            body = "Bridge and spot-dl generated playlist imports require NAS Bridge (MVP 2+).",
            sectionTag = "NAS BRIDGE REQUIRED",
        )
        Text(
            text = "TODO: create playlist · add track · M3U import/export · spot-dl playlist import",
            style = MaterialTheme.typography.bodySmall,
            color = LocalIalemusTokens.current.textMuted,
        )
    }
}

@Composable
private fun AudiobooksBrowsePane(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    currentTrackId: String?,
    modifier: Modifier = Modifier,
) {
    val audiobooks by libraryViewModel.audiobookTracks.collectAsStateWithLifecycle()
    if (audiobooks.isEmpty()) {
        EmptyLibraryState(
            title = "No audiobook signals detected",
            body = "Classifies tracks over 20 minutes or paths containing audiobook/books folders.",
            sectionTag = "AUDIOBOOKS",
            modifier = modifier,
            actions = {
                HudStatusChip(label = "TODO: resume · chapters · speed", disabled = true)
            },
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = ListBottomPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        itemsIndexed(audiobooks, key = { _, track -> track.id }) { index, track ->
            TrackRow(
                track = track,
                isPlaying = track.id == currentTrackId,
                onClick = { playerViewModel.playTrack(audiobooks, track) },
                index = index,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SignalBrowsePane(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    currentTrackId: String?,
    modifier: Modifier = Modifier,
) {
    var signalMode by rememberSaveable { mutableStateOf(SignalBrowseMode.FAVORITES) }
    val favorites by libraryViewModel.favoriteTracks.collectAsStateWithLifecycle()
    val recentAdded by libraryViewModel.recentlyAddedTracks.collectAsStateWithLifecycle()
    val recentPlayed by libraryViewModel.recentlyPlayedTracks.collectAsStateWithLifecycle()
    val mostPlayed by libraryViewModel.mostPlayedTracks.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SignalBrowseMode.entries.forEach { mode ->
                Box(modifier = Modifier.clickable { signalMode = mode }) {
                    HudStatusChip(
                        label = mode.label,
                        highlighted = signalMode == mode,
                    )
                }
            }
        }
        val tracks = when (signalMode) {
            SignalBrowseMode.FAVORITES -> favorites
            SignalBrowseMode.RECENTLY_ADDED -> recentAdded
            SignalBrowseMode.RECENTLY_PLAYED -> recentPlayed
            SignalBrowseMode.MOST_PLAYED -> mostPlayed
        }
        TrackListPane(
            tracks = tracks,
            currentTrackId = currentTrackId,
            onTrackClick = { track -> playerViewModel.playTrack(tracks, track) },
            emptyTitle = when (signalMode) {
                SignalBrowseMode.FAVORITES -> "No favorites yet"
                SignalBrowseMode.RECENTLY_ADDED -> "No recently added tracks"
                SignalBrowseMode.RECENTLY_PLAYED -> "No play history yet"
                SignalBrowseMode.MOST_PLAYED -> "No play counts yet"
            },
            emptyBody = when (signalMode) {
                SignalBrowseMode.FAVORITES -> "Mark tracks as favorite from Now Playing."
                SignalBrowseMode.RECENTLY_ADDED -> "Newly scanned tracks appear here by date added."
                SignalBrowseMode.RECENTLY_PLAYED -> "Play tracks to build listening history."
                SignalBrowseMode.MOST_PLAYED -> "Repeated plays will rank tracks here."
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TrackListPane(
    tracks: List<Track>,
    currentTrackId: String?,
    onTrackClick: (Track) -> Unit,
    emptyTitle: String,
    emptyBody: String,
    modifier: Modifier = Modifier,
) {
    if (tracks.isEmpty()) {
        EmptyLibraryState(
            title = emptyTitle,
            body = emptyBody,
            sectionTag = "TRACK INDEX",
            modifier = modifier,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = ListBottomPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
            TrackRow(
                track = track,
                isPlaying = track.id == currentTrackId,
                onClick = { onTrackClick(track) },
                index = index,
            )
        }
    }
}

@Composable
private fun ArtistDetailPane(
    artist: String,
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    currentTrackId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tracks by libraryViewModel.tracksForArtist(artist).collectAsStateWithLifecycle()
    DetailScaffold(
        title = artist,
        subtitle = "${tracks.size} tracks",
        onBack = onBack,
        onPlayAll = { playerViewModel.playCollection(tracks) },
        onShuffle = { playerViewModel.playCollection(tracks, shuffle = true) },
        modifier = modifier,
    ) {
        TrackListPane(
            tracks = tracks,
            currentTrackId = currentTrackId,
            onTrackClick = { track -> playerViewModel.playTrack(tracks, track) },
            emptyTitle = "No tracks for artist",
            emptyBody = "This artist has no indexed tracks.",
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun AlbumDetailPane(
    album: String,
    artist: String,
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    currentTrackId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tracks by libraryViewModel.tracksForAlbum(album, artist).collectAsStateWithLifecycle()
    DetailScaffold(
        title = album,
        subtitle = "$artist · ${tracks.size} tracks",
        onBack = onBack,
        onPlayAll = { playerViewModel.playCollection(tracks) },
        onShuffle = { playerViewModel.playCollection(tracks, shuffle = true) },
        modifier = modifier,
    ) {
        TrackListPane(
            tracks = tracks,
            currentTrackId = currentTrackId,
            onTrackClick = { track -> playerViewModel.playTrack(tracks, track) },
            emptyTitle = "No tracks for album",
            emptyBody = "This album has no indexed tracks.",
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun FolderDetailPane(
    librarySourceId: String,
    folderPath: String,
    displayName: String,
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    currentTrackId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tracks by libraryViewModel.tracksForFolder(librarySourceId, folderPath).collectAsStateWithLifecycle()
    DetailScaffold(
        title = displayName,
        subtitle = "${tracks.size} tracks",
        onBack = onBack,
        onPlayAll = { playerViewModel.playCollection(tracks) },
        onShuffle = { playerViewModel.playCollection(tracks, shuffle = true) },
        modifier = modifier,
    ) {
        TrackListPane(
            tracks = tracks,
            currentTrackId = currentTrackId,
            onTrackClick = { track -> playerViewModel.playTrack(tracks, track) },
            emptyTitle = "No tracks in folder",
            emptyBody = "This folder path has no indexed tracks.",
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun DetailScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HudPanel(title = title, sectionTag = "TRACK INDEX", subtitle = subtitle) {
            RowActions(onBack = onBack, onPlayAll = onPlayAll, onShuffle = onShuffle)
        }
        content()
    }
}

@Composable
private fun RowActions(
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HudButton(label = "Back", onClick = onBack, accent = com.heathen.ialemus.ui.components.HudButtonAccent.Neutral)
        HudButton(label = "Play All", onClick = onPlayAll)
        HudButton(label = "Shuffle", onClick = onShuffle, accent = com.heathen.ialemus.ui.components.HudButtonAccent.Warning)
    }
}
