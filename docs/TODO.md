# Ialemus — Task Checklist

Actionable tasks organized by MVP phase. This checklist complements `IALEMUS_MVP_ROADMAP.md` and `DECISIONS.md`.

**Rule:** Android scaffold is initialized. See Development section in `README.md` for build commands.

---

## MVP 0 — Documentation, scaffold planning, themes, architecture validation

### Documentation (current pass)

- [x] `IALEMUS_PROJECT_SPEC.md` — product spec
- [x] `IALEMUS_ARCHITECTURE.md` — architecture overview
- [x] `IALEMUS_MVP_ROADMAP.md` — phase summary
- [x] `DECISIONS.md` — architecture decision record
- [x] `NAS_BRIDGE_SPEC.md` — bridge API and safety
- [x] `ANDROID_APP_SPEC.md` — app modules and layouts
- [x] `TODO.md` — this file
- [x] `.env.example` — placeholder environment variables
- [ ] Resolve open architecture questions (see bottom of this file)

### Scaffold planning

- [x] Confirm package name: `com.heathen.ialemus`
- [ ] Confirm DI choice: Hilt vs Koin
- [ ] Confirm HTTP client: Retrofit vs Ktor
- [x] Draft module layout: single `app` module for MVP 0–1 (split later if needed)
- [x] Document navigation graph: Now Playing, Library, Acquire, Downloads, Settings
- [x] Register theme enum names (all eight themes from `DECISIONS.md` ADR-005)
- [x] Define minimum Android SDK: API 26 (`minSdk`), target API 35
- [ ] Plan signing / APK release workflow for HiBy R4 sideload

### Architecture validation

- [ ] Review bridge safety rules with NAS deployment reality
- [ ] Confirm Ugreen NAS paths match `.env.example` placeholders
- [ ] Validate spot-dl / MeTube / slskd service names on NAS
- [ ] Decide: stream vs copy-to-DAP for NAS playback
- [x] User approval to initialize Android project

### Android project init

- [x] Create Kotlin + Jetpack Compose project
- [x] Add Media3, Room, DataStore, Coil (WorkManager/networking deferred)
- [x] Local music permission flow
- [x] Install debug APK on Android device / HiBy R4 (manual)

### Next steps (post-scaffold)

- [x] Verify debug APK builds (`./gradlew assembleDebug`)
- [ ] Install and verify on HiBy R4 with real music library
- [x] Add local playback with Media3 / ExoPlayer + MediaSessionService (MVP 1A)
- [ ] Migrate to Navigation Compose for deep links and back stack

---

## MVP 1A — Local scan + playback (completed)

- [x] MediaStore scan with 30s duration filter
- [x] Room schema: `TrackEntity`, `TrackStatsEntity`
- [x] Permission flow (`READ_MEDIA_AUDIO` / legacy read)
- [x] Library screen: real track list, tap-to-play
- [x] Media3 + `IalemusPlaybackService` (`MediaSessionService`)
- [x] Play / pause / seek / previous / next
- [x] In-memory queue + queue sheet
- [x] Mini player bar
- [x] Now Playing with album art (Coil), seek bar, action placeholders
- [x] Favorite toggle (Room)
- [x] ShuffleEngine stub (OFF / TRUE_RANDOM / REPEAT_QUEUE / REPEAT_ONE)
- [x] DataStore theme + DAP mode toggle
- [x] Settings: local track count, MVP 1A build label

## MVP 1A Hotfix — Playback + folder scan + EVA UI (completed)

- [x] Fix tap-to-play always starting first track
- [x] SAF folder picker + persisted sources (Room `library_sources`)
- [x] Scan selected folders only by default
- [x] Full-device MediaStore scan explicit opt-in
- [x] EVA-inspired theme system (7 themes) + preserve 8 Ialemus themes
- [x] EVA-01 Berserk default theme
- [x] HUD panels, status chips, themed Library/Now Playing/Mini Player
- [x] `PlaybackIndexMapper` unit test

## MVP 1A EVA HUD Pass — Interface overhaul (completed)

- [x] Reusable HUD design layer (`IalemusThemeTokens`, `HudShapes`, `HudTypography`)
- [x] HUD components: `HudScaffold`, `HudPanel`, `HudHeader`, `HudSectionLabel`, `HudButton`, `HudIconButton`, `HudStatusChip`, `HudBottomNavigation`, `HudDivider`, `HudBackground`
- [x] Custom command-dock bottom navigation (replaces Material `NavigationBar`)
- [x] Now Playing flagship screen — loaded + rich empty state (inactive audio core panel)
- [x] Library / `MusicSourcePanel` command-console styling; tactical `TrackRow` index
- [x] Mini player bar with transport + shuffle/repeat controls (HiBy hotfix)
- [x] Settings HUD pass — EVA themes first, DAP mode explanation, MVP 1A EVA HUD label
- [x] Acquire / Downloads placeholder restyle with future-module status chips
- [x] Playback stability guard — snackbar on tap-to-play failure; no silent swallow
- [x] DAP mode disables grid/scanlines for battery-friendly visuals
- [x] No official/copyrighted Evangelion assets used

### Known limitations (EVA HUD pass)

- NAS Bridge, spot-dl, MeTube, slskd remain disabled placeholders
- Heavy blur / infinite animations intentionally omitted for battery
- Navigation still state-based tabs (not Navigation Compose deep links)

## MVP 1B — Library navigation + HUD usability + widget (completed)

- [x] Icons-only command dock (accessibility contentDescription preserved)
- [x] Collapsible Music Source panel (auto-collapses after scan)
- [x] Prominent Library Browser / Track Index with full-height lists
- [x] Browse modes: Tracks (real), Artists (real), Albums (real), Folders (real)
- [x] Browse scaffolds: Genres, Playlists, Audiobooks (classification heuristic)
- [x] Signal index: Favorites, Recently Added, Recently Played, Most Played
- [x] Artist/Album/Folder detail views with Play All / Shuffle
- [x] Source management in Settings (shared `LibraryViewModel` / repository)
- [x] Collapsible Settings sections (themes, NAS, source management)
- [x] `HudCollapsiblePanel`, `MusicSourceControls`, `LibraryBrowseRows`
- [x] Room DAO aggregation queries (artists, albums, folders)
- [x] Android widget scaffold (`IalemusPlaybackWidgetProvider`, RemoteViews HUD layout)
- [x] Widget state sync from playback (title/artist/playing)
- [x] Downloads job-queue collapsible panels (style preserved)

## HiBy R4 Hotfix — Stability + mini player + layout (completed)

- [x] Safe next/previous transport (`PlaybackTransport`, bounds checks, try/catch)
- [x] Separate `RepeatMode` (OFF / QUEUE / ONE) from shuffle toggle
- [x] Player error snackbar — *"Playback failed. Try rescanning this source."* / *"Track unavailable. Rescan or reselect the source."*
- [x] Debug logging for queue index, transport, repeat/shuffle (no sensitive paths)
- [x] `PlaybackTransportTest` unit tests
- [x] Spotify-style persistent mini player (shuffle, prev, play/pause, next, repeat)
- [x] Mini player tap body → Now Playing; buttons use separate click targets
- [x] Library defaults to track list; source panel collapsed when tracks/sources exist
- [x] `SOURCES` chip, horizontal scrollable browse tabs, search, Play All / Shuffle All
- [x] Compact layout constants (`CompactLayout.kt`) for HiBy R4 width
- [x] Bottom dock `weight(1f)` tabs; screen horizontal padding; text ellipsis
- [ ] Manual HiBy R4 acceptance test on device (see README install command)

## MVP 1B.1 — Dock polish + NAS connector UI (completed)

- [x] Spotify-style bottom dock (less squished on HiBy R4)
- [x] NAS / Bridge Connections settings (DataStore)
- [x] MeTube URL connector UI + external browser open
- [x] slskd URL connector UI + external browser open
- [x] spotDL Bridge-only job form scaffold (no Android execution)
- [x] HTTP connection test helper (`ServiceUrlTester`)
- [x] Downloads page job category placeholders
- [x] `POST /jobs/spotdl/playlist` draft in `NAS_BRIDGE_SPEC.md`
- [ ] Encrypted token storage (TODO)
- [ ] In-app WebView for LAN services (future — external browser for now)

## MVP 1B.2 — Docker Web UI wrappers (completed)

- [x] In-app WebView for MeTube, slskd, Ugreen NAS UI
- [x] `ServiceWebViewScreen` with EVA HUD header, back/refresh/external browser
- [x] Settings NAS / Docker Web UIs with local defaults and Reset
- [x] URL validation (`ServiceUrlValidator`) — http/https only
- [x] Acquire service cards: Open in Ialemus + external browser
- [x] Mini player hidden during WebView for full height
- [x] spotDL remains Bridge-only scaffold
- [ ] Bridge job submission (MVP 2)

## MVP 1B.4 — EVA contrast + Spotify scaffold (completed)

- [x] EVA-01 / EVA theme contrast — explicit Material on-* colors via `hudDarkColorScheme`
- [x] `HudOutlinedTextField` for readable text fields on HUD themes
- [x] Now Playing icon-only transport + action rows
- [x] Streaming tab (replaces Acquire/shopping cart)
- [x] Spotify settings + login/playthrough scaffold (`docs/SPOTIFY_INTEGRATION_PLAN.md`)
- [x] Docker modules moved to Downloads (MeTube, slskd, NAS UI, spotDL)
- [x] MeTube WebView render warning + debug panel + desktop UA
- [ ] Manual HiBy R4 acceptance on device
- [ ] Audit contrast for every theme before release

### Known limitations (MVP 1B.4)

- Spotify PKCE auth not fully wired — login opens browser scaffold only; tokens not stored yet
- Spotify App Remote dependency not added — remote controls are UI scaffold
- Spotify playback never flows through Ialemus ExoPlayer (by design)
- MeTube may still fail to render in WebView on some devices — use external browser fallback
- spotDL remains Bridge-only on Downloads page

## MVP 1B.3 — Now Playing layouts + metadata (completed)

- [x] Remove redundant Now Playing top banner
- [x] Hide mini player on Now Playing screen by default
- [x] Settings toggle: Show bottom mini player (`showMiniPlayerBar`)
- [x] Five Now Playing layout modes (Balanced, Image Heavy, Text + Metadata, Playlist / Radio, Cyberpunk HUD)
- [x] Collapsible panels: Local Signal, Queue, Track Cleanup, Lyrics placeholder, future tools
- [x] Local Signal metadata panel upgrade (source, dates, IDs, favorite, play count)
- [x] Display title override + prefix cleanup (`TrackOverrideEntity` in Room v3)
- [x] Reset display override; physical file rename deferred (disabled future options)
- [x] MeTube/NAS default IP URLs (`192.168.1.213`); avoid `baphomet.local` as default
- [x] WebView desktop user-agent + error code display
- [ ] Manual HiBy R4 acceptance on device

### Known limitations (MVP 1B.3)

- Display title override is Room-only — does not rename files or rewrite embedded tags
- Physical rename / tag edit shown as future disabled options
- Codec, bitrate, sample rate, ReplayGain shown as TODO placeholders
- Radio mode is UI placeholder only (no smart queue algorithm yet)
- Room v3 uses destructive migration — overrides/stats reset on upgrade until proper migration added
- MeTube/NAS WebView load depends on LAN reachability and service WebView compatibility
- `baphomet.local` requires LAN DNS/mDNS if user enters it manually

## WebView Hotfix — Docker service rendering (completed)

- [x] Fix WebView zero-height layout (`Modifier.weight(1f)` in Column)
- [x] White WebView background; remove purple-only empty panel
- [x] `usesCleartextTraffic=true` + LAN domain network security config
- [x] WebViewClient error/HTTP error handling with Retry + external browser
- [x] `normalizeForLoad()` for host:port URLs without scheme
- [x] Load status line in HUD header

### Known limitations (WebView hotfix)

- WebView does not inject JS interfaces or store credentials/cookies manually
- slskd/MeTube login handled by normal web session in WebView
- No job scraping from web UIs into Downloads yet
- Bridge URL field is future/optional — not required for Docker wrappers
- `baphomet.local` hostname requires LAN DNS/mDNS on device

### Known limitations (MVP 1B.1)

- Web UI opens in external browser (not in-app WebView)
- Bridge token stored in DataStore (not encrypted yet)
- spotDL Submit to Bridge disabled until Bridge MVP 2
- No actual job polling from Bridge yet
- Cleartext HTTP permitted for LAN testing (`network_security_config.xml`)

### Known limitations (HiBy hotfix)

- Widget transport controls remain open-app only
- Genres/playlists/audiobooks still scaffolds (no full indexing)
- Navigation still state-based tabs (not Navigation Compose)
- SAF tracks may lack artist/album metadata until tag extraction

### Known limitations (MVP 1B)

- Genres: not indexed yet (honest empty state; TODO MediaStore/metadata retriever)
- Playlists: UI scaffold only (no Room playlist schema yet)
- Audiobooks: heuristic classification only (no resume/chapters/speed/bookmarks)
- Widget: open-app only; no MediaSession transport from widget yet
- SAF tracks may lack artist/album metadata until tag extraction is added
- Navigation still state-based (no Nav Compose back stack for detail drill-down)

### MVP 1B — Remaining polish (future)

- [ ] Richer queue UI (remove/reorder)
- [ ] Favorite persistence polish across screens
- [ ] Play count threshold polish (50% / 4 min rule)
- [ ] Local lyrics panel
- [ ] Widget
- [ ] Landscape two-pane Now Playing
- [ ] WorkManager background rescan
- [ ] Incremental MediaStore scan (API 30+ generation markers)
- [ ] Storage Access Framework folder grants

---

## MVP 1 — Local library scanning and playback (remaining)

### Library scanner

- [ ] MediaStore scan
- [ ] Storage Access Framework folder grants
- [ ] Metadata extraction (title, artist, album, duration, artwork)
- [ ] Room schema: `Track`, `TrackStats`, albums/artists views
- [ ] Background rescan via WorkManager
- [ ] Library tabs: Tracks, Artists, Albums, Playlists, Folders

### Player core

- [ ] Media3 + `MediaSessionService`
- [ ] Play / pause / seek / skip
- [ ] Queue management
- [ ] Repeat one / repeat all
- [ ] Notification and lock-screen controls
- [ ] Bluetooth / media button support

### Now Playing screen

- [ ] Portrait layout: art, metadata, progress, controls
- [ ] Source chip (Local)
- [ ] Queue entry point

### Shuffle engine (v1)

- [ ] Pure Chaos mode
- [ ] Shuffle session seed display
- [ ] Reshuffle action

### Favorites and play counts

- [ ] Favorite toggle per track
- [ ] Play count rules (50% or 4 min threshold)
- [ ] Recently played list
- [ ] Low play count / never played filters

### Settings (MVP 1 subset)

- [ ] Playback preferences
- [ ] Library scanner settings
- [ ] Shuffle mode default
- [ ] Theme picker (basic)

---

## MVP 2 — NAS Bridge connection and remote library import

### Connection settings

- [ ] Ialemus Bridge URL + token (encrypted storage)
- [ ] Test connection / health check UI
- [ ] Ugreen NAS display name and base URL fields
- [ ] Storage path configuration (read from bridge or local overrides)

### Bridge client

- [ ] `GET /health`
- [ ] `GET /library/recent`
- [ ] `GET /library/browse`
- [ ] `POST /library/rescan`
- [ ] Bearer token auth on all requests

### Remote library

- [ ] Import NAS-indexed tracks into Room
- [ ] Source type: `NAS_INDEXED`, `NAS_STREAM`
- [ ] Stream or play NAS tracks via ExoPlayer
- [ ] Manual rescan trigger
- [ ] Recently added from bridge

### Library UI updates

- [ ] Sources filter (local vs NAS)
- [ ] Origin metadata where available

---

## MVP 3 — spot-dl GUI and job history

### Acquire screen (spot-dl)

- [ ] Paste Spotify URL / search input
- [ ] Job type: track, album, playlist
- [ ] Profile selector (allowlisted names from bridge)
- [ ] Output target and format options
- [ ] Submit via `POST /jobs`

### Job tracking

- [ ] `GET /jobs` list with status filters
- [ ] `GET /jobs/{id}` detail
- [ ] `POST /jobs/{id}/cancel`
- [ ] Job cards: service, status, progress, timestamps, errors

### Downloads screen

- [ ] Completed imports section
- [ ] Failed jobs section
- [ ] Post-complete: rescan, play now, favorite all

### Integration

- [ ] Folder watcher notifications (poll or SSE)
- [ ] New tracks appear in library after job completion

---

## MVP 4 — MeTube and slskd/Soulseek integrations

### MeTube

- [ ] URL submit (audio / video / profile)
- [ ] Job status via bridge MeTube profile
- [ ] Completed MeTube import to music or video library
- [ ] Optional: open external MeTube web UI

### slskd / Soulseek

- [ ] Search UI (if bridge exposes search proxy)
- [ ] Download queue status
- [ ] Completed slskd folder import
- [ ] Fallback: open external slskd web UI
- [ ] Respect slskd API auth via bridge only

### Acquire screen updates

- [ ] Tabs or sections: spot-dl, MeTube, slskd
- [ ] Unified job history across services

---

## MVP 5 — Widget, landscape polish, DAP battery mode

### Widget

- [ ] Small: title, artist, play/pause, next
- [ ] Medium: art, controls, favorite
- [ ] Large: queue count, shuffle chip, optional lyrics line
- [ ] Widget themes: match app, minimal black, Ghost in the Code, Terminal Kittie

### Landscape layouts

- [ ] Now Playing: art + controls | lyrics/waveform | queue context
- [ ] Library: adaptive master-detail where width allows
- [ ] Acquire / Downloads: two-pane job list + detail

### Themes and HUD polish

- [ ] All eight themes visually complete
- [ ] HUD density: clean / normal / overload
- [ ] Scanline intensity control
- [ ] Album art blur background toggle

### HiBy R4 / DAP mode

- [ ] DAP low-power mode toggle
- [ ] Disable animations toggle
- [ ] Wi-Fi-only bridge sync
- [ ] Reduced polling interval
- [ ] Default theme recommendation: Archive Black
- [ ] OLED-friendly pure dark surfaces

### Lyrics (if not done earlier)

- [ ] Embedded + sidecar `.lrc` / `.txt`
- [ ] Full-screen and mini overlay on Now Playing
- [ ] Landscape lyrics panel

---

## Open architecture questions

Answer these before Android project initialization where possible:

1. **Default NAS hostname** — What LAN hostname or IP should docs/examples use? (Currently placeholder only.)
2. **spot-dl deployment** — Exact container/service name on Ugreen NAS? Bridge-managed or pre-existing?
3. **Playback strategy** — Stream from NAS vs copy favorites/albums to DAP storage?
4. **MeTube scope** — Audio-only import vs in-app video playback?
5. **Jellyfin** — Direct API integration later, or folder-only via bridge?
6. **Soulseek UX** — Full in-app search via bridge proxy, or completed-downloads-only?
7. **Default theme** — Ghost in the Code, Chthonic Signal, or Archive Black for first launch?
8. **Bridge runtime** — Node/Fastify, Python/FastAPI, or Go for `ialemus-bridge`?
9. **Job notifications** — Polling interval vs WebSocket/SSE from bridge?
10. **Package / signing** — Confirm `com.heathen.ialemus` and release signing approach for HiBy sideload.
