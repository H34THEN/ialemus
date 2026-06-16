# Ialemus — Android Music + Media Player Project Spec

**Working name:** Ialemus  
**Name source:** Ialemus / Ialemos, a lesser Greek figure associated with lament, dirge, and song.  
**Project type:** Android music + media player for HiBy R4 DAP and Android phones  
**Primary build target:** Android 12+ device compatibility, with special attention to HiBy R4 DAP behavior  
**Secondary target:** Standard Android phone with home-screen widget support  
**Initial deliverable:** Cursor-ready planning/spec document before project setup

---

## 1. Product Vision

Ialemus is a cyber-occult Android music and media player built for a self-hosted music ecosystem.

The app should work as a beautiful local/offline player first, while also acting as a controlled GUI front-end for remote media acquisition and indexing workflows running on the user’s Ugreen NAS. It should connect to the user’s existing spot-dl, MeTube, Soulseek/slskd, Jellyfin-style storage, and Ugreen network shares.

The app should feel like a “personal signal console” for music: part DAP player, part media terminal, part library ritual system, with practical controls and polished HUD overlays.

The app must never require cloud accounts for core playback. It should prioritize local files, LAN access, NAS storage, and user-owned/self-hosted services.

---

## 2. Core Goals

1. Build a full Android music and media player usable on:
   - HiBy R4 DAP
   - Android phone
   - Landscape and portrait layouts

2. Allow the app to trigger NAS-side media jobs through a GUI:
   - spot-dl job launcher
   - MeTube job launcher
   - slskd/Soulseek search + download status where practical
   - Optional generic “Docker command profile” runner through a safe NAS-side command broker

3. Automatically detect, import, and expose completed results:
   - Watch configured NAS download folders
   - Scan local device storage
   - Sync or stream files from NAS
   - Refresh app library after downloads complete

4. Provide a strong player experience:
   - Local playback
   - NAS playback
   - Queue management
   - Favorites
   - Play counts
   - Recently played
   - True randomized shuffle
   - Lyrics where available
   - Tag/metadata views
   - Album artist browsing
   - Playlist support
   - Optional offline caching

5. Provide fun but functional UI:
   - HUD overlays
   - Multiple themes
   - Visual signal panels
   - Landscape mode as a first-class layout
   - Home-screen widget

---

## 3. Important Compatibility Notes

### HiBy R4

The HiBy R4 is an Android-based DAP. Design should target Android 12+ behavior, avoid assuming Google Play Services are always available, and keep the app installable from APK sideloading or GitHub Releases.

### Android Phone

The app should support normal Android phones with:
- Notification controls
- Lock-screen controls
- MediaSession integration
- Bluetooth controls
- Home-screen widget
- Background playback
- Storage picker support
- Landscape mode

---

## 4. Safety, Legal, and Scope Boundaries

The app should only help the user manage and access media they are authorized to download, archive, or play.

The app should not:
- Circumvent DRM.
- Steal account credentials.
- Embed private service passwords in the APK.
- Expose NAS command execution directly to the internet.
- Download copyrighted media without user authorization.
- Store slskd, spot-dl, MeTube, or NAS credentials in plaintext.
- Run arbitrary shell commands from the Android device without a restricted allowlist.

The recommended architecture is **Android app → authenticated NAS-side API broker → Docker/service integrations**, not Android app → raw SSH root command execution.

---

## 5. Proposed Architecture

```text
┌──────────────────────────────────────────────────────────────┐
│ Android Device                                                │
│ HiBy R4 / Phone                                               │
│                                                              │
│  Ialemus App                                                  │
│  - Player UI                                                  │
│  - Local scanner                                              │
│  - NAS browser                                                │
│  - Job launcher GUI                                           │
│  - Queue / shuffle / favorites / play counts                  │
│  - Lyrics display                                             │
│  - Widget + notification controls                             │
└──────────────────────────────┬───────────────────────────────┘
                               │ HTTPS/LAN API
                               │ token auth
                               ▼
┌──────────────────────────────────────────────────────────────┐
│ Ugreen NAS                                                    │
│                                                              │
│  Ialemus Bridge / Command Broker                              │
│  - Authenticated API                                          │
│  - Allowlisted command profiles                               │
│  - Job queue                                                  │
│  - Download folder watcher                                    │
│  - Library scanner endpoint                                   │
│  - Service health checks                                      │
│                                                              │
│  Integrations:                                                │
│  - spot-dl Docker/container/command profile                   │
│  - MeTube / yt-dlp web service                                │
│  - slskd / Soulseek                                           │
│  - Jellyfin/media folders                                     │
│  - SMB/NFS/local NAS paths                                    │
└──────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────┐
│ Music Storage                                                 │
│                                                              │
│  /volume1/Jellyfin/data/...                                   │
│  /volume1/Jellyfin/data/MeTube/...                            │
│  /volume1/Jellyfin/data/Soulseek_Inbox/downloads/...          │
│  /volume1/Music or configured music library path              │
└──────────────────────────────────────────────────────────────┘
```

---

## 6. Recommended Tech Stack

### Android App

Preferred approach:

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Playback:** AndroidX Media3 / ExoPlayer
- **Audio session:** MediaSessionService
- **Database:** Room
- **Background jobs:** WorkManager
- **Networking:** Ktor Client or Retrofit + OkHttp
- **Serialization:** Kotlinx Serialization
- **Dependency injection:** Hilt or Koin
- **Image loading:** Coil
- **Storage access:** Android Storage Access Framework + MediaStore
- **Widget:** Glance App Widget or classic RemoteViews widget
- **Lyrics:** Embedded tags first, sidecar `.lrc` second, configured lyrics provider third
- **Settings:** DataStore Preferences + encrypted token storage

### NAS Bridge

Preferred approach:

- **Runtime:** Node.js/Fastify, Python/FastAPI, or Go
- **Deployment:** Docker Compose on Ugreen NAS
- **Auth:** Token-based auth with rotating API tokens
- **Command execution:** Strict allowlisted job templates only
- **File indexing:** Watch configured folders and expose completed jobs
- **Optional:** WebSocket/SSE for live job status

Recommended initial bridge name:

```text
ialemus-bridge
```

---

## 7. Major App Modules

### 7.1 Player Core

Features:
- Play/pause/seek/skip
- Queue
- Repeat one/repeat all
- True randomized shuffle
- Gapless playback where supported
- ReplayGain support if available
- Crossfade optional later
- Sleep timer
- Resume position for long tracks
- Bluetooth headset/media button support
- Notification player controls
- Lock-screen controls

Implementation notes:
- Use Media3 as the playback foundation.
- Keep player state in a single playback service.
- UI should subscribe to player state rather than own playback state directly.

---

### 7.2 Library Scanner

Sources:
- Local device music folders
- Android MediaStore
- User-selected folders via Storage Access Framework
- NAS indexed library from Ialemus Bridge
- Optional SMB/NFS path mounting if device supports access through a file provider
- Optional Jellyfin API later

Library metadata:
- Track title
- Artist
- Album artist
- Album
- Genre
- Year
- Track number
- Disc number
- Duration
- Bitrate/sample rate when available
- File path / URI
- Source type: local, cached, NAS, stream, completed-job
- Artwork path / embedded artwork
- Lyrics availability
- Play count
- Favorite status
- Last played timestamp
- Date added
- Origin: spot-dl, metube, slskd, manual import, unknown

---

### 7.3 NAS Connection Settings

Settings should be very easy to access.

Required settings page:

```text
Settings
└── Connections
    ├── Ugreen NAS
    │   ├── NAS display name
    │   ├── LAN base URL
    │   ├── Remote/VPN base URL optional
    │   ├── API token
    │   ├── Test connection button
    │   ├── Health check result
    │   └── Last successful sync
    │
    ├── Ialemus Bridge
    │   ├── Bridge URL
    │   ├── Token
    │   ├── Service status
    │   ├── Job history endpoint status
    │   └── Folder watcher status
    │
    ├── spot-dl
    │   ├── Enabled toggle
    │   ├── Command profile selector
    │   ├── Default output folder
    │   ├── Default format
    │   ├── Playlist handling behavior
    │   ├── Metadata/lyrics toggles
    │   └── Test job button
    │
    ├── MeTube
    │   ├── Enabled toggle
    │   ├── Base URL
    │   ├── Default output folder
    │   ├── Audio-only default toggle
    │   ├── Video download toggle
    │   └── Test connection button
    │
    ├── slskd / Soulseek
    │   ├── Enabled toggle
    │   ├── Base URL
    │   ├── API key/token
    │   ├── Downloads folder
    │   ├── Search enabled toggle
    │   └── Test connection button
    │
    └── Storage Paths
        ├── Music library root
        ├── MeTube completed downloads
        ├── Soulseek completed downloads
        ├── spot-dl completed downloads
        ├── Local cache folder
        └── Rescan library button
```

Security:
- Store sensitive tokens with Android EncryptedSharedPreferences or equivalent.
- Never log tokens.
- Redact tokens in export/debug logs.
- Offer “copy redacted diagnostics” button.

---

## 8. Job Launcher GUI

### 8.1 spot-dl GUI

Screen name options:
- “Acquire”
- “Signal Pull”
- “spot-dl Console”
- “Playlist Ritual”

User flow:
1. Paste Spotify URL or search query.
2. Choose job type:
   - Track
   - Album
   - Playlist
   - Saved command profile
3. Choose output target:
   - NAS music library
   - spot-dl staging folder
   - device local cache after completion
4. Choose format:
   - MP3
   - M4A
   - OPUS
   - Use NAS default
5. Toggle metadata:
   - Album art
   - Lyrics
   - M3U playlist generation
   - Skip existing
6. Submit job.
7. Show live status:
   - queued
   - running
   - downloading
   - tagging
   - completed
   - failed
8. On complete:
   - Rescan library
   - Show new tracks
   - Offer “play now”
   - Offer “favorite all”
   - Offer “add to playlist”

Important:
- Android should not execute the raw Docker command locally.
- The Android app should call the Ialemus Bridge, which runs a safe allowlisted spot-dl job profile on the NAS.

Example bridge request:

```json
{
  "type": "spotdl",
  "url": "https://open.spotify.com/playlist/example",
  "profile": "music-m4a-lyrics",
  "outputTarget": "nas_music",
  "skipExisting": true
}
```

Example bridge command template:

```bash
docker exec spotdl spotdl download "$URL" \
  --output "/music/{artist}/{album}/{title}.{output-ext}" \
  --format m4a \
  --lyrics genius \
  --generate-m3u \
  --skip-existing
```

Cursor should treat this as a placeholder. The final command must match the actual NAS container/service name, mounted folders, and installed spot-dl options.

---

### 8.2 MeTube GUI

User flow:
1. Paste URL.
2. Select:
   - audio only
   - video
   - best available
   - custom profile
3. Choose output folder.
4. Submit to MeTube or bridge.
5. Track status.
6. Import completed media.

Recommended views:
- Queue
- Completed
- Failed
- “Import to Music”
- “Import to Videos”

---

### 8.3 slskd / Soulseek GUI

User flow:
1. Search query.
2. Show results.
3. Filter:
   - file type
   - bitrate
   - folder
   - user
   - size
4. Start download.
5. Show queue and status.
6. On complete, import/download folder scan.

Important:
- Respect slskd API auth.
- Never expose slskd directly without token auth.
- Offer “open in external slskd web UI” as fallback.

---

### 8.4 Generic Command Profiles

Provide an advanced settings section for named, allowlisted command profiles.

Example:

```yaml
profiles:
  spotdl_default:
    label: "spot-dl Default Music Pull"
    service: "spotdl"
    allowed_args:
      - url
      - format
      - outputTarget
      - skipExisting
    command_template: "docker exec spotdl spotdl download {{url}} --format {{format}} --skip-existing"
```

Rules:
- No arbitrary command input from app.
- No freeform shell text.
- Each variable must be validated.
- URLs must be parsed and escaped.
- Commands must run as a low-privilege user.
- Job logs should redact secrets.

---

## 9. Shuffle System

The app needs a strong “true randomized” shuffle system that feels better than basic random.

Modes:

### Pure Chaos
- Cryptographically strong random seed.
- Every track has equal chance.
- No artist/album balancing.

### Smart Chaos
- Avoid immediate repeats.
- Avoid same artist too close together.
- Avoid same album too close together.
- Optionally weight favorites slightly higher.
- Optionally include “forgotten tracks” that have low play counts.

### Deep Cut Ritual
- Favor low-play-count tracks.
- Favor tracks not played recently.
- Avoid top favorites unless user toggles them in.

### Favorite Storm
- Shuffle only favorites or heavily weight favorites.

Implementation:
- Use a generated shuffle session with a saved seed.
- Store shuffle history to avoid accidental repeats.
- Allow “reshuffle now.”
- Allow “freeze this queue.”
- Show tiny HUD line: `RNG SEED: 7F4A-91C2 | MODE: SMART CHAOS`.

---

## 10. Play Counts + Favorites

Track-level stats:
- play_count
- skip_count
- favorite boolean
- last_played_at
- first_played_at
- date_added
- total_listen_time_ms
- completion_count
- source_origin
- mood tag optional later

Play count rules:
- Count a play when:
  - 50% of track is completed, or
  - at least 4 minutes are played for long tracks
- Do not count repeated seeking abuse multiple times in a few minutes.
- Store play stats locally.
- Optional sync to NAS bridge later.

Favorites:
- Favorite track
- Favorite album
- Favorite artist
- Favorite playlist
- “Bloodmark” / “Sigil” UI label option for theme flavor, but database should use normal terms.

---

## 11. Lyrics

Priority order:
1. Embedded synced lyrics
2. Sidecar `.lrc`
3. Sidecar `.txt`
4. Metadata lyrics from spot-dl output
5. Optional online lyrics provider later

Lyrics UI:
- Full-screen lyrics mode
- Mini lyrics overlay on Now Playing
- Landscape lyrics panel
- Synced line highlight if timestamps available
- Unsynced scroll mode
- “No lyrics found” with option to locate sidecar file

---

## 12. HUD / Visual Design

Ialemus should feel cyberpunk, gothic, and functional.

UI motifs:
- Signal scanlines
- Spectrogram panels
- Dossier cards
- Terminal readouts
- Neon edge highlights
- Status chips
- Occult glyph-inspired but readable icons
- Album art as central “artifact”
- Animated waveform / fake oscilloscope optional
- Library stats as “signal telemetry”

Avoid:
- Overly busy UI that hurts usability
- Tiny unreadable text
- Excessive animation on DAP hardware
- Battery-heavy visual effects by default

Performance:
- Provide “Low Power HUD” toggle.
- Provide “Disable animations” toggle.
- Provide “DAP battery mode.”

---

## 13. Theme Options

Initial themes:

### Ghost in the Code
- Dark translucent panels
- Blue-green ghost glow
- Soft glitch overlays
- Faint terminal grids

### Terminal Kittie
- Cute terminal hacker theme
- Green phosphor text
- Cat-ear glyph accents
- Friendly status labels

### Chthonic Signal
- Hades Watch aligned
- Black, ember, crimson, bone-white
- Underworld transmission aesthetic

### Neon Ossuary
- Bone-white typography
- Violet, cyan, magenta accents
- Gothic UI borders

### Archive Black
- Minimal, battery-friendly
- Pure dark background
- Thin white/gray UI
- Best for OLED and DAP use

### Fallout CRT
- Amber/green CRT terminal look
- Scanline optional
- Chunky old-machine panels

### Cyber Shrine
- Sacred-tech feel
- Gold, black, red accents
- Ritual panel transitions

### Candy Malware
- Bright corrupted bubblegum cyberpunk
- Pink, blue, acid green
- Optional for phone use, not default for DAP

Theme settings:
- Global theme
- Accent color override
- Font scale
- Reduce animation
- Scanline intensity
- HUD density: clean / normal / overload
- Album art blur background toggle

---

## 14. Screens

### 14.1 Home / Now Playing

Portrait:
- Album art
- Track title
- Artist
- Album
- Progress bar
- Main controls
- Favorite button
- Queue button
- Shuffle mode chip
- Lyrics chip
- Source chip: Local / NAS / Cache / Stream
- HUD overlay with bitrate/sample rate if known

Landscape:
- Left: album art + controls
- Center: waveform/lyrics/current track
- Right: queue/up-next/library context
- Bottom: progress + transport controls

---

### 14.2 Library

Tabs:
- Tracks
- Artists
- Albums
- Playlists
- Folders
- Sources
- Recent
- Favorites
- Downloads / Acquisitions

Filters:
- Source
- Format
- Favorite
- Recently added
- Low play count
- Never played
- Lyrics available
- Origin: spot-dl / MeTube / slskd / manual / local

---

### 14.3 Acquire / Downloads

Sections:
- spot-dl
- MeTube
- slskd
- Job History
- Completed Imports
- Failed Jobs

Each job card:
- Service
- URL/search query
- Status
- Progress
- Output folder
- Created time
- Completion time
- Error log if failed
- Import/play actions

---

### 14.4 Settings

Must be practical and easy.

Top-level:
- Connections
- Storage
- Playback
- Shuffle
- Library Scanner
- Lyrics
- Themes
- Widget
- Security
- Diagnostics
- About

Diagnostics:
- Test NAS bridge
- Test MeTube
- Test slskd
- Test spot-dl command profile
- Show storage permissions
- Show library count
- Export redacted logs
- Show app version/build hash

---

## 15. Widget

The Android home-screen widget should include:

Small widget:
- Track title
- Artist
- Play/pause
- Next

Medium widget:
- Album art
- Track title
- Artist
- Play/pause
- Previous/next
- Favorite button

Large widget:
- Album art
- Track/artist/album
- Playback controls
- Shuffle mode
- Favorite
- Current queue count
- Optional lyrics line

Widget themes:
- Match app theme
- Minimal black
- Ghost in the Code
- Terminal Kittie

---

## 16. Data Model Draft

### Track

```kotlin
data class Track(
    val id: String,
    val title: String,
    val artist: String?,
    val albumArtist: String?,
    val album: String?,
    val genre: String?,
    val year: Int?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val durationMs: Long,
    val uri: String,
    val sourceType: SourceType,
    val origin: TrackOrigin,
    val artworkUri: String?,
    val lyricsUri: String?,
    val dateAdded: Long,
    val lastScannedAt: Long
)
```

### TrackStats

```kotlin
data class TrackStats(
    val trackId: String,
    val playCount: Int,
    val skipCount: Int,
    val completionCount: Int,
    val totalListenTimeMs: Long,
    val favorite: Boolean,
    val firstPlayedAt: Long?,
    val lastPlayedAt: Long?
)
```

### RemoteJob

```kotlin
data class RemoteJob(
    val id: String,
    val service: RemoteService,
    val status: JobStatus,
    val input: String,
    val profile: String,
    val outputTarget: String,
    val progressPercent: Double?,
    val createdAt: Long,
    val completedAt: Long?,
    val errorMessage: String?
)
```

Enums:
- SourceType: LOCAL, NAS_INDEXED, NAS_STREAM, DEVICE_CACHE
- TrackOrigin: MANUAL, SPOTDL, METUBE, SLSKD, JELLYFIN, UNKNOWN
- RemoteService: SPOTDL, METUBE, SLSKD, GENERIC_PROFILE
- JobStatus: QUEUED, RUNNING, COMPLETED, FAILED, CANCELLED

---

## 17. Ialemus Bridge API Draft

Base URL:

```text
http://ugreen-nas.local:PORT/api
```

All endpoints require:

```text
Authorization: Bearer <token>
```

### Health

```http
GET /health
```

Response:

```json
{
  "ok": true,
  "version": "0.1.0",
  "services": {
    "spotdl": "ok",
    "metube": "ok",
    "slskd": "ok",
    "watcher": "ok"
  }
}
```

### Create Job

```http
POST /jobs
```

Request:

```json
{
  "service": "spotdl",
  "profile": "music-m4a-lyrics",
  "input": "https://open.spotify.com/playlist/example",
  "options": {
    "skipExisting": true,
    "outputTarget": "nas_music"
  }
}
```

### List Jobs

```http
GET /jobs
```

### Read Job

```http
GET /jobs/{id}
```

### Cancel Job

```http
POST /jobs/{id}/cancel
```

### Library Rescan

```http
POST /library/rescan
```

### Recently Added

```http
GET /library/recent?limit=100
```

### Browse Folder

```http
GET /library/browse?path=/volume1/Jellyfin/data/Music
```

---

## 18. Folder Watch Strategy

The bridge should watch configured folders:

```text
/volume1/Jellyfin/data/MeTube
/volume1/Jellyfin/data/Soulseek_Inbox/downloads
/volume1/Jellyfin/data/Music
/volume1/Music
```

The exact defaults must be configurable because the user’s Ugreen setup may evolve.

When a new file appears:
1. Wait until file size is stable.
2. Extract metadata.
3. Add to bridge index.
4. Notify app through polling or WebSocket/SSE.
5. App imports new record into local Room database.

---

## 19. Offline / Local Cache

The app should offer:
- Stream from NAS
- Download to device
- Cache playlist
- Cache album
- Cache favorites
- Cache recently added
- Clear cache
- Show cache size
- Respect device storage limits

DAP mode:
- Prefer local files and local cache.
- Avoid constant polling.
- Sync only on Wi-Fi.
- Battery-friendly library refresh.

---

## 20. Permissions

Android permissions:
- Audio/media access permissions depending on Android version
- Notification permission on Android 13+
- Foreground service permission as needed
- Network access
- Optional wake lock for playback
- Storage Access Framework folder grants

Avoid unnecessary permissions:
- No camera
- No microphone
- No contacts
- No location

---

## 21. MVP Scope

### MVP 0 — Spec + Project Skeleton

Deliver:
- Android Kotlin/Compose project
- App name Ialemus
- Package name suggestion: `com.heathen.ialemus`
- Basic theme system scaffold
- Navigation scaffold
- Settings scaffold
- Local music permission flow
- Basic player placeholder

### MVP 1 — Local Player

Deliver:
- Scan local music
- Display library
- Play tracks
- Now Playing screen
- Queue
- Shuffle mode v1
- Favorites
- Play counts
- Notification controls

### MVP 2 — NAS Bridge Integration

Deliver:
- Connection settings
- Bridge health check
- Recently added endpoint
- Remote library import
- Stream/play NAS indexed tracks
- Manual rescan

### MVP 3 — spot-dl GUI

Deliver:
- spot-dl job form
- Submit job to bridge
- Job status list
- Completed job import
- “Play new tracks” action

### MVP 4 — MeTube + slskd

Deliver:
- MeTube submit/status/import
- slskd search/status/import if API access is stable
- Fallback “open external web UI” buttons

### MVP 5 — Widget + Landscape Polish

Deliver:
- Phone widget
- Landscape now-playing layout
- Theme polish
- Low-power DAP mode

---

## 22. Cursor Implementation Instructions

Cursor should begin by creating documentation only, then wait for the user before initializing the Android project.

First task:
1. Create `/docs/IALEMUS_PROJECT_SPEC.md`.
2. Copy this full spec into that file.
3. Create `/docs/IALEMUS_ARCHITECTURE.md` with a concise architecture diagram and decisions.
4. Create `/docs/IALEMUS_MVP_ROADMAP.md` with the MVP phases.
5. Do not create the Android project yet unless explicitly instructed.
6. Do not add secrets, real tokens, or raw NAS passwords.
7. Mark all NAS URLs and tokens as placeholders.
8. Add a note that command execution must go through a restricted authenticated NAS-side bridge, not raw app-side shell access.

Second task, only after user approval:
1. Initialize Android project.
2. Use Kotlin + Jetpack Compose.
3. Add Media3 dependencies.
4. Add Room, DataStore, WorkManager, and networking dependencies.
5. Add app navigation structure:
   - Now Playing
   - Library
   - Acquire
   - Downloads
   - Settings
6. Add theme enum stubs:
   - Ghost in the Code
   - Terminal Kittie
   - Chthonic Signal
   - Neon Ossuary
   - Archive Black
   - Fallout CRT
   - Cyber Shrine
   - Candy Malware
7. Add placeholder settings screens for all connection paths.

---

## 23. Open Questions For Later

Do not block the documentation phase on these.

1. What exact Ugreen NAS hostname/IP should be the default?
2. What exact spot-dl container/service name is running?
3. Is spot-dl already containerized, or should Ialemus Bridge create/manage that container?
4. Should downloads be copied to the DAP automatically or streamed from NAS?
5. Should the app support video playback for MeTube results or only import audio?
6. Should Jellyfin be integrated directly, or should Ialemus only read the underlying folders?
7. Should the app expose Soulseek search directly or only show completed slskd downloads?
8. Which theme should be the default: Ghost in the Code or Chthonic Signal?

---

## 24. Design Tone

Ialemus should feel like:

- A music player for a haunted cyberpunk archive
- A DAP interface made for ritual listening
- A functional NAS-connected acquisition console
- A self-hosted alternative to locked-down streaming apps
- A clean Android app with enough personality to feel alive

The UI should be stylish, but the music controls must remain fast, readable, and reliable.
