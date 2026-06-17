# Ialemus MVP Roadmap

Phased delivery plan for Ialemus. Actionable checklists live in `TODO.md`.

**Prerequisite:** Documentation and architecture validation (MVP 0) complete before Android project initialization.

---

## MVP 0 — Documentation, scaffold planning, themes, architecture validation

**Goal:** Documentation-first foundation; no Android source generated until approved.

Deliverables:

- Product spec, architecture, decisions, bridge spec, app spec
- Theme name registry (eight themes)
- `.env.example` placeholders
- Scaffold planning: package name, modules, navigation, stack choices
- Open architecture questions resolved or explicitly deferred

**Not in scope yet:** Gradle project, Kotlin sources, Compose UI code.

---

## MVP 1 — Local library scanning and playback

**Goal:** Fully usable offline/local player.

### MVP 1A (shipped)

- MediaStore scanner with Room persistence
- SAF folder-first scan; full-device scan explicit opt-in
- Permission flow for local audio access
- Library track list with tap-to-play (stable track ID → queue index)
- Media3 / `IalemusPlaybackService` playback
- Now Playing, mini player, queue sheet
- Favorites (Room), DataStore settings
- **EVA HUD Pass:** Custom HUD design system, EVA-01 Berserk default theme, command-dock navigation, playback error snackbar guard

### MVP 1B (shipped)

- Album / artist / folder browse from Room aggregation
- Artist/album/folder detail views with Play All / Shuffle
- Signal index: favorites, recently added/played, most played
- Genres / Playlists / Audiobooks scaffolds (honest limitations documented)
- Icons-only command dock; collapsible Music Source and Settings modules
- Source management in Settings (shared repository)
- Android home-screen widget scaffold (HUD RemoteViews)

### HiBy R4 Hotfix (shipped — `0.3.1-hiby-hotfix`)

- Safe next/previous transport — no crash at queue bounds; repeat queue wraps; repeat one repeats current track
- Spotify-style persistent mini player above command dock (shuffle, prev, play/pause, next, repeat)
- Library click-depth reduction — tracks default tab, collapsed source panel, search, Play All / Shuffle All
- HiBy R4 responsive layout — compact width detection, scrollable tabs, weighted dock, no side clipping
- Playback error snackbars; `PlaybackTransport` unit tests; debug-only transport logging

### MVP 1B.1 — Dock polish + NAS connectors (shipped — `0.3.2-mvp1b1`)

- Spotify-style bottom navigation (single HUD bar, active pill, roomy touch targets)
- NAS / Bridge Connections in Settings (DataStore URLs + token)
- Acquire: MeTube/slskd URL cards, external browser Web UI, connection tests
- spotDL Bridge-only playlist job form (disabled until Bridge MVP 2)
- Downloads job category placeholders aligned with Job Queue style

### MVP 1B.2 — Docker Web UI wrappers (shipped — `0.3.3-mvp1b2`)

- In-app WebView wrappers for MeTube, slskd, Ugreen NAS UI
- Settings NAS / Docker Web UIs with local LAN defaults
- Acquire Open in Ialemus + external browser; spotDL still Bridge-only
- No shell/SSH/Docker from Android

### WebView Hotfix (shipped — `0.3.4-webview-hotfix`)

- Fixed Compose layout collapse that showed purple HUD box without webpage
- WebView fills remaining screen height; cleartext LAN HTTP enabled
- External browser fallback on load failure

### MVP 1B.3 — Now Playing layouts + metadata (shipped — `0.3.5-mvp1b3`)

- Removed redundant Now Playing header; mini player hidden on Now Playing by default
- Settings: show bottom mini player toggle; five Now Playing layout modes
- Local Signal metadata panel; Track Cleanup display title override (Room-only)
- Default service URLs use LAN IP (`192.168.1.213`) not `baphomet.local`
- WebView desktop user-agent for MeTube; error codes + external browser fallback

### MVP 1B.4 — EVA contrast + Spotify scaffold (shipped — `0.3.6-mvp1b4`)

- EVA theme contrast fix (`hudDarkColorScheme`, `HudOutlinedTextField`)
- Now Playing icon-only controls
- Streaming tab + Spotify integration scaffold
- Docker Web UI modules on Downloads page
- MeTube WebView render warning + debug panel

### MVP 1B.9 — ANR hardening, reactive visualizer, lyrics (shipped — `0.3.13-mvp1b9`)

- Playback/scan performance hardening; debug StrictMode
- Reactive audio visualizer (session attach + simulated fallback)
- Image Heavy safe-area fix
- Lyrics foundation (Room + Now Playing panel)

### MVP 1B.8 fix — library persistence + visualizers (shipped — `0.3.12-mvp1b8-fix`)

- Safe rescan preserves Room tracks when SAF unreachable
- Image Heavy / Playlist-Radio / Cyberpunk HUD layout fixes
- Six Cyberpunk visualizer modes + DAP static fallback

### MVP 1C — Next (planned)

Playlist deepening, audiobook mode, genre/artist/album polish — see `docs/TODO.md` MVP 1C section.

### MVP 1B.8 — Now Playing usability + playlists (shipped — `0.3.11-mvp1b8`)

- Above-fold transport controls in all Now Playing layouts
- Empty-state sync actions (Sync All / Sync Folder) on Now Playing; Library SOURCES preserved
- Local Room playlists + M3U import; metadata/audio tools expansion
- Downloads + Settings modules collapsed by default

### MVP 1B.7 — Spotify hidden, themes, MeTube WebView (shipped — `0.3.9-mvp1b7`)

- Spotify visually deprecated (hidden dock tab; Settings experimental section)
- Distinct Ialemus Original theme palettes + preview dots
- EVA hex HUD accents
- MeTube WebView diagnostics and fallback UX

### MVP 1B.6 — Spotify App Remote (shipped — `0.3.8-mvp1b6`)

- Spotify app detection + Open Spotify App / Web
- App Remote SDK integration (`SpotifyRemoteRepository`)
- HiBy R4 device activation UX on Streaming tab
- Web API device list + transfer playback helper
- Dual SPOTIFY REMOTE controls (App Remote + Web API fallback)

### MVP 1B.5 — Spotify PKCE login (shipped — `0.3.7-mvp1b5`)

- PKCE auth, deep link callback, token refresh, profile/playback Web API
- Prefilled personal Client ID; no Client Secret in Android

### MVP 1C / remaining polish (next)

Deliverables (full MVP 1):

- Local library scanner (MediaStore, SAF)
- Room database and library UI
- Player core (Media3, MediaSessionService)
- Now Playing screen (portrait)
- Queue, shuffle v1, favorites, play counts
- Notification and lock-screen controls

---

## MVP 2 — NAS Bridge connection and remote library import

**Goal:** Connect to Ialemus Bridge and play/import NAS-indexed media.

Deliverables:

- Bridge connection settings (URL, encrypted token)
- Health check and diagnostics
- `GET /library/recent`, browse, rescan
- Remote library import into Room
- Stream or play NAS tracks
- Source filters in library UI

---

## MVP 3 — spot-dl GUI and job history

**Goal:** Acquisition UI for spot-dl through bridge only.

Deliverables:

- Acquire screen: spot-dl job form
- `POST /jobs`, list/detail/cancel
- Downloads screen: job history, completed/failed
- Post-job rescan and “play new tracks”

---

## MVP 4 — MeTube and slskd/Soulseek integrations

**Goal:** Extend acquisition to MeTube and slskd via bridge.

Deliverables:

- MeTube submit, status, import
- slskd search/download status (or completed-downloads mode)
- Unified job history across services
- Fallback links to external web UIs

---

## MVP 5 — Widget, landscape polish, DAP battery mode

**Goal:** Phone widget and HiBy R4 polish.

Deliverables:

- Home-screen widget (small/medium/large)
- Landscape Now Playing and adaptive library/acquire layouts
- All eight themes visually complete
- DAP low-power mode: reduced animation, Wi-Fi-only sync, Archive Black default
- Lyrics panels in landscape

---

## Document map

| Phase | Primary specs |
|-------|----------------|
| All | `DECISIONS.md`, `IALEMUS_ARCHITECTURE.md` |
| Bridge work | `NAS_BRIDGE_SPEC.md`, `.env.example` |
| App work | `ANDROID_APP_SPEC.md`, `IALEMUS_PROJECT_SPEC.md` |
| Tasks | `TODO.md` |
