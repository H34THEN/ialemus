# Ialemus — Android App Specification

**App name:** Ialemus  
**Package (proposed):** `com.heathen.ialemus`  
**Targets:** HiBy R4 DAP, Android phones  
**Minimum SDK:** API 26 (`minSdk`), target API 35  
**Distribution:** Sideload/APK friendly; no Google Play Services required for core playback

**Implementation status (MVP 1A EVA HUD Pass):** Local SAF folder scan + opt-in MediaStore scan, Room library, Media3 playback, full EVA-inspired HUD UI layer. NAS/Bridge/Acquire remain placeholders. Version `0.2.2-mvp1a-eva-hud`.

See also: `DECISIONS.md`, `IALEMUS_PROJECT_SPEC.md`, `NAS_BRIDGE_SPEC.md`.

---

## 1. Architectural boundary

The Android app is a **client** for playback, library management, and acquisition **UI**. It communicates with the NAS exclusively through **Ialemus Bridge** HTTP APIs for jobs, rescans, and remote library data.

The app **must not**:

- Execute raw shell commands
- Open SSH sessions to the NAS
- Run arbitrary Docker commands
- Embed NAS root credentials

---

## 2. Technology stack

| Concern | Technology |
|---------|------------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Playback | AndroidX Media3 / ExoPlayer |
| Background audio | `MediaSessionService` |
| Database | Room |
| Preferences | DataStore Preferences |
| Tokens / secrets | Encrypted settings storage |
| Background work | WorkManager |
| Networking | Retrofit + OkHttp or Ktor Client |
| Artwork | Coil |
| Widget | Glance or RemoteViews |
| DI | Manual `AppContainer` (Hilt/Koin TBD) |

---

## 3. App modules (logical)

These may map to Gradle modules or packages within a single `app` module for early MVPs.

### 3.1 Player core

**Responsibility:** Single source of truth for playback state.

Features:

- Play, pause, seek, skip previous/next
- Queue: add, remove, reorder, clear
- Repeat one / repeat all / off
- Gapless where supported
- Sleep timer (later)
- Resume position for long tracks
- `MediaSession` for notification, lock screen, Bluetooth, widget
- ExoPlayer data sources: local file, content URI, NAS HTTP/stream URI

**Implementation rule:** UI subscribes to player state from the playback service; screens do not own playback directly.

---

### 3.2 Library scanner

**Responsibility:** Index local and imported media into Room.

Sources:

- Android MediaStore
- Storage Access Framework folder grants
- Bridge-imported NAS index (`GET /library/recent`, browse, rescan)

Metadata per track:

- Title, artist, album artist, album, genre, year
- Track/disc number, duration, bitrate/sample rate when available
- URI, source type, origin (spot-dl, metube, slskd, manual, local)
- Artwork URI, lyrics availability
- Date added, last scanned

Background rescans via WorkManager.

---

### 3.3 Now Playing screen

**Responsibility:** Primary playback HUD.

**Portrait:**

- Album art (center “artifact”)
- Title, artist, album
- Progress bar and elapsed/remaining time
- Transport controls
- Favorite, queue, lyrics chips
- Shuffle mode chip
- Source chip: Local / NAS / Cache / Stream
- Optional HUD line: bitrate, sample rate, RNG seed

**Landscape (first-class, polished in MVP 5):**

```text
┌──────────────┬─────────────────────┬──────────────┐
│ Album art    │ Waveform / lyrics   │ Queue /      │
│ + transport  │ + track metadata    │ up-next /    │
│              │                     │ context      │
├──────────────┴─────────────────────┴──────────────┤
│ Progress bar + main transport controls            │
└───────────────────────────────────────────────────┘
```

---

### 3.4 Library screen

**Responsibility:** Browse and filter indexed media.

Tabs:

- Tracks, Artists, Albums, Playlists, Folders
- Sources, Recent, Favorites
- Downloads / Acquisitions (link to Downloads screen)

Filters:

- Source (local, NAS, cache)
- Format, favorite, recently added
- Low play count, never played
- Lyrics available
- Origin: spot-dl / MeTube / slskd / manual / local

Adaptive layout: single column portrait; master-detail or wider grids in landscape on tablets/phones.

---

### 3.5 Acquire screen

**Responsibility:** GUI for starting NAS-side acquisition jobs via bridge.

Sections (expand by MVP):

| MVP | Services |
|-----|----------|
| MVP 3 | spot-dl |
| MVP 4 | MeTube, slskd/Soulseek |

**spot-dl flow:**

1. Paste Spotify URL or query
2. Select job type: track, album, playlist
3. Choose allowlisted profile and output target
4. Toggle: skip existing, lyrics, format
5. Submit `POST /jobs`
6. Show live status from `GET /jobs/{id}`

**MeTube flow:** URL, audio/video profile, submit, track, import.

**slskd flow:** Search (if enabled), download, queue status, import; fallback link to external slskd web UI.

Screen naming in UI may use flavor labels (“Signal Pull”, “Playlist Ritual”) while navigation graph uses `Acquire`.

---

### 3.6 Downloads screen

**Responsibility:** Job history and post-download actions.

Sections:

- Active jobs
- Completed imports
- Failed jobs

Job card fields:

- Service, input URL/query, profile, status, progress
- Output folder, created/completed time
- Redacted error summary
- Actions: cancel, retry, rescan library, play now, favorite all, add to playlist

---

### 3.7 Settings screen

**Responsibility:** Connections, playback, library, themes, diagnostics.

Top-level groups:

```text
Settings
├── Connections (NAS, Ialemus Bridge, spot-dl, MeTube, slskd)
├── Storage paths and cache
├── Playback
├── Shuffle defaults
├── Library scanner
├── Lyrics
├── Themes
├── Widget
├── Security (encrypted token info, redacted export)
├── Diagnostics (test bridge, test services, permissions, library count)
└── About
```

Sensitive fields (bridge token, API keys) use encrypted storage. Never log plaintext tokens.

---

### 3.8 Widget

**Responsibility:** Home-screen control without opening app.

| Size | Content |
|------|---------|
| Small | Title, artist, play/pause, next |
| Medium | Art, title, artist, prev/play/next, favorite |
| Large | Art, metadata, shuffle chip, queue count, optional lyrics line |

Widget themes: match app theme, minimal black, Ghost in the Code, Terminal Kittie.

---

### 3.9 Themes

Eight named themes (enum stubs in MVP 0; visual polish through MVP 5):

1. **Ghost in the Code** — dark translucent panels, blue-green glow, terminal grids
2. **Terminal Kittie** — green phosphor, cat-ear accents, friendly labels
3. **Chthonic Signal** — ember, crimson, bone-white, underworld transmission
4. **Neon Ossuary** — bone-white type, violet/cyan/magenta, gothic borders
5. **Archive Black** — minimal OLED dark; **recommended DAP default**
6. **Fallout CRT** — amber/green CRT, optional scanlines
7. **Cyber Shrine** — gold, black, red, ritual panels
8. **Candy Malware** — bright cyberpunk; better for phone than DAP

Theme settings: global theme, accent override, font scale, reduce animation, scanline intensity, HUD density (clean / normal / overload), album art blur background.

---

### 3.10 Lyrics

**Priority:** Embedded synced → sidecar `.lrc` → `.txt` → spot-dl metadata → online provider (later).

**UI:** Full-screen mode, mini Now Playing overlay, landscape panel, synced highlight or scroll.

---

### 3.11 Shuffle engine

**Modes:**

| Mode | Behavior |
|------|----------|
| Pure Chaos | CSPRNG seed; equal weight |
| Smart Chaos | Avoid artist/album repeats; optional favorite weighting |
| Deep Cut Ritual | Favor low play count and stale tracks |
| Favorite Storm | Favorites-only or heavy favorite bias |

Persist shuffle session seed; show HUD line e.g. `RNG SEED: 7F4A-91C2 | MODE: SMART CHAOS`. Support reshuffle and freeze queue.

---

### 3.12 Favorites and play counts

**TrackStats (Room):**

- `play_count`, `skip_count`, `completion_count`
- `favorite`, `first_played_at`, `last_played_at`
- `total_listen_time_ms`

**Play count rule:** Count when 50% complete or 4 minutes played (whichever comes first for long tracks). Debounce seek abuse.

Favorites at track, album, artist, playlist level. Optional UI flavor labels (“Bloodmark”, “Sigil”); DB uses standard terms.

---

## 4. Navigation structure

Primary bottom or rail navigation (exact chrome TBD):

```text
Now Playing | Library | Acquire | Downloads | Settings
```

Now Playing may be default landing or merge with mini-player bar on Library — decide at UI scaffold.

---

## 5. HiBy R4 and sideload considerations

| Requirement | Approach |
|-------------|----------|
| Android 12+ | `minSdk 31` |
| Sideload / APK | No hard dependency on Play Store or GMS |
| Core playback offline | Local files + optional NAS stream; no cloud account |
| DAP low-power mode | Reduced animation, less bridge polling, Wi-Fi-only sync |
| Battery | Default to Archive Black; disable heavy HUD effects |
| Storage | Prefer local library + selective cache on DAP |
| Permissions | Media access, notifications (API 33+), network, FGS as needed — no camera/mic/location |

---

## 6. Portrait and landscape expectations

| Screen | Portrait | Landscape |
|--------|----------|-----------|
| Now Playing | Stacked art + controls | Three-pane HUD (art / center / queue) |
| Library | Tabbed list | Master-detail or grid |
| Acquire | Form + recent jobs list | Form + job detail side-by-side |
| Downloads | Job list | List + detail pane |
| Settings | Grouped list | Two-column where width allows |
| Lyrics | Full-screen overlay | Dedicated center panel |

All layouts must remain readable on HiBy R4 screen size; avoid tiny text and excessive animation in DAP mode.

---

## 7. Data layer (summary)

**Room entities:** `Track`, `TrackStats`, `Playlist`, `RemoteJob` (cached from bridge), album/artist views.

**DataStore:** User preferences, theme, shuffle defaults, non-secret toggles.

**Encrypted storage:** Bridge token, optional per-service keys if ever needed on device (prefer bridge-side slskd keys).

**Bridge sync:** WorkManager periodic or on-demand import; no shell.

---

## 8. Permissions (target)

- Read audio/media (version-specific scoped storage APIs)
- `INTERNET` for bridge and streaming
- `POST_NOTIFICATIONS` (API 33+)
- Foreground service types for media playback
- SAF folder URI persistence

**Not required:** Camera, microphone, contacts, location.

---

## 9. MVP mapping

| Module | MVP 0 | MVP 1 | MVP 2 | MVP 3 | MVP 4 | MVP 5 |
|--------|-------|-------|-------|-------|-------|-------|
| Player core | plan | ✓ | stream | ✓ | ✓ | ✓ |
| Library scanner | plan | local | +NAS | +jobs | +imports | ✓ |
| Now Playing | plan | ✓ | ✓ | ✓ | ✓ | landscape |
| Library | plan | ✓ | +NAS | filters | origins | polish |
| Acquire | plan | — | — | spot-dl | +MeTube/slskd | ✓ |
| Downloads | plan | — | — | jobs | unified | ✓ |
| Settings | plan | subset | bridge | jobs | services | DAP mode |
| Widget | plan | — | — | — | — | ✓ |
| Themes | names | basic | ✓ | ✓ | ✓ | all eight |
| Lyrics | plan | optional | ✓ | ✓ | ✓ | landscape |
| Shuffle | plan | v1 | ✓ | ✓ | ✓ | all modes |
| Favorites / counts | plan | ✓ | ✓ | ✓ | ✓ | ✓ |

---

## 10. Out of scope for initial app spec

- Jellyfin API direct integration (folder-only via bridge for now)
- Video player for MeTube (optional later)
- Cloud sync or streaming service accounts
- Arbitrary NAS command profiles from user free text
