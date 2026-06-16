# Ialemus

Android music and media player for HiBy R4 DAP, Android phones, and self-hosted NAS media workflows.

Ialemus connects to a Ugreen NAS media stack through **Ialemus Bridge** — a restricted, authenticated API on the NAS. The Android app never runs raw shell, SSH, or arbitrary Docker commands. Acquisition (spot-dl, MeTube, slskd/Soulseek), folder watching, and library rescans go through allowlisted bridge endpoints.

**Features (planned):** local and NAS music, play counts, favorites, strong shuffle, lyrics, themes, widgets, portrait/landscape layouts, and polished DAP low-power mode.

**Status:** MVP 1B.5 — Spotify PKCE login with prefilled personal-app defaults.

### MVP 1B.5 (`0.3.7-mvp1b5`)

- **Spotify PKCE login** — Authorization Code + S256 challenge; deep link `ialemus://spotify-auth-callback`
- **Prefilled defaults** — Client ID + Redirect URI ready on first run (personal app; no Client Secret)
- **Token exchange + refresh** — profile fetch; currently-playing / player Web API; remote play/pause/next/previous
- **Streaming tab** — Connect Spotify, profile, remote playback card, SPOTIFY REMOTE controls
- **Preserved:** LOCAL CORE playback, folder-first scan, Downloads Docker modules, HiBy stability

### MVP 1B.4 (`0.3.6-mvp1b4`)

- **EVA contrast fix** — explicit `hudDarkColorScheme()` on-* colors; `HudOutlinedTextField` for readable labels/text on dark HUD surfaces
- **Now Playing icon controls** — shuffle, previous, play/pause, next, repeat in one row; secondary action icon row (favorite, queue, lyrics, metadata, cleanup, more)
- **Streaming tab** — replaces Acquire/shopping cart; Spotify account + remote playback scaffold (SPOTIFY REMOTE, not LOCAL CORE)
- **Spotify settings** — Client ID, Redirect URI, login/logout scaffold, Open Spotify app
- **Docker modules in Downloads** — MeTube, slskd, NAS UI, spotDL modules with in-app WebView; Settings retains URL configuration
- **MeTube WebView** — desktop UA for MeTube/NAS, 12s render warning + debug panel, external browser fallback
- **Preserved:** local playback, folder-first scan, slskd WebView, HiBy stability

### MVP 1B.3 (`0.3.5-mvp1b3`)

- **Now Playing overhaul** — removed redundant top “Now Playing” banner; content-first Spotify-like layout
- **Mini player visibility** — Settings toggle “Show bottom mini player” (DataStore `showMiniPlayerBar`, default on); hidden on Now Playing screen by default
- **Five layout modes** — Balanced, Image Heavy, Text + Metadata, Playlist / Radio, Cyberpunk HUD (Settings → Playback)
- **Local Signal metadata panel** — title, artist, album, source, duration, dates, IDs, favorite/play count; technical URI behind collapsible section
- **Track Cleanup** — display title override in Room (`TrackOverrideEntity`); strip `01 - ` style prefixes; reset override; no physical file rename
- **Service URL defaults** — MeTube `http://192.168.1.213:38245/`, slskd `http://192.168.1.213:5031`, NAS UI `http://192.168.1.213:9999/` (IP preferred over `baphomet.local`)
- **WebView** — desktop Chrome user-agent for MeTube compatibility; visible error codes + Open External Browser fallback
- **Preserved:** local playback, folder-first scan, slskd WebView, HiBy dock polish

### WebView Hotfix (`0.3.4-webview-hotfix`)

- **Fixed purple empty box** — WebView container now uses `Modifier.weight(1f)` so it receives real height inside Compose Column
- **Real AndroidView WebView** — white background, JS/DOM storage, wide viewport, mixed content compatibility
- **Cleartext LAN HTTP** — `usesCleartextTraffic=true` + targeted network security config for `192.168.1.213` (IP defaults); `baphomet.local` optional user override only
- **Error UX** — visible load status, HTTP errors, Retry + Open External Browser fallback
- **URL normalization** — `normalizeForLoad()` prepends `http://` when scheme missing

### MVP 1B.2 (`0.3.3-mvp1b2`)

- **In-app WebView wrappers** — MeTube, slskd, and Ugreen NAS UI open inside Ialemus with EVA HUD chrome (back, refresh, external browser)
- **Settings: NAS / Docker Web UIs** — configurable URLs with local defaults, Save, Reset to Local Defaults, per-service connection tests
- **Acquire service cards** — Open in Ialemus, external browser, Test, Edit URL; spotDL remains Bridge-only/future
- **No Bridge required** — wraps existing Docker container web UIs; no shell/SSH/Docker from Android
- **Preserved:** local playback, folder-first scan, HiBy dock polish, mini player (hidden during WebView)

### MVP 1B.1 (`0.3.2-mvp1b1`)

- **Spotify-style bottom dock** — single semi-transparent HUD bar; active pill indicator; roomy 48dp touch targets; icon-only on HiBy R4 compact width
- **NAS / Bridge Connections** — DataStore settings for Bridge URL/token, MeTube URL, slskd URL, Jellyfin placeholder, connection mode
- **Connection tests** — safe HTTP GET tests with status chips (Not configured / Ready / Checking / Reachable / Failed)
- **Acquire connectors** — MeTube and slskd cards with Open Web UI (external browser), Test, Save URL; spotDL Bridge-only job form (disabled submit)
- **Downloads job categories** — Bridge Jobs, MeTube Imports, slskd Downloads, spotDL Playlist Jobs, Completed, Failed (placeholders)
- **Preserved:** HiBy playback stability (`PlaybackTransport`), mini player controls, folder-first scan, EVA HUD themes

### HiBy R4 Hotfix (`0.3.1-hiby-hotfix`)

- **Next/previous crash fix** — safe queue bounds checks, null-safe track lookup, try/catch on transport; repeat queue wraps; repeat one repeats current track; empty queue no-ops; player errors show snackbar instead of force-close
- **Spotify-style mini player** — persistent bar above command dock when a track is loaded; tap body opens Now Playing; separate controls for shuffle, previous, play/pause, next, repeat (off → queue → one)
- **Library click-depth reduction** — track list visible immediately after scan; Music Source panel collapsed by default when sources/tracks exist; compact `SOURCES` chip; horizontal scrollable browse tabs; quick search; Play All / Shuffle All on Tracks
- **HiBy R4 responsive layout** — compact width detection (`screenWidthDp < 420`), weighted bottom dock tabs, horizontal padding, ellipsis text, scrollable library tabs; content bottom padding for mini player + dock
- **Debug logging** — queue size, index, transport actions, repeat/shuffle, mediaId, player errors (debug builds only; no full paths)
- **Unit tests** — `PlaybackTransportTest` for end-of-queue, repeat wrap, empty queue
- **Preserved:** folder-first SAF scan, full-device opt-in only, EVA HUD themes, local playback

### MVP 1B

- **Icons-only command dock** — bottom nav shows icons only; selected tab label appears above dock
- **Library browser** — prominent Track Index with tabs: Tracks, Artists, Albums, Genres, Playlists, Folders, Audiobooks, Signal
- **Real browse data** — Artists, Albums, Folders from Room aggregation; artist/album/folder detail views with Play All / Shuffle
- **Signal index** — Favorites, Recently Added, Recently Played, Most Played
- **Collapsible modules** — Music Source panel collapses after scan; Settings themes/NAS/source sections collapsible
- **Source management in Settings** — same SAF folder controls as Library (shared repository logic)
- **Android widget scaffold** — semi-transparent EVA HUD home-screen widget (opens app; shows current track when playing)
- **Preserved:** folder-first SAF scan, tap-to-play, EVA HUD themes, playback stability guard

### MVP 1A EVA HUD Pass

- **EVA HUD interface:** Custom command-console styling — angular panels, neon outlines, grid/scanline motif (DAP mode disables overlays)
- **Default theme:** EVA-01 Berserk (violet / acid green / orange accents)
- **HUD design layer:** `HudScaffold`, `HudPanel`, `HudBottomNavigation`, `HudButton`, `TrackRow`, `MiniPlayerBar`, `MusicSourcePanel`, theme tokens
- **Screens restyled:** Now Playing (rich empty state), Library, Settings, Acquire, Downloads, mini player, bottom nav
- **Playback stability guard:** Tap-to-play errors surface snackbar — *"Playback link failed. Try rescanning this source."* (debug logs only in `BuildConfig.DEBUG`)
- **Legal/style:** Original EVA-inspired colors and HUD shapes only — no official Evangelion/NERV logos, marks, or copyrighted assets
- **Preserved:** Folder-first SAF scan, full-device opt-in only, tap-to-play queue index logic unchanged

### MVP 1A Hotfix (base)

- **Fixed:** Tapping any library track now starts that exact track (not always the first file)
- **Folder-first scan:** Choose Music Folder via SAF before scanning; no auto full-device scan
- **Full Device Music Scan:** Explicit opt-in only; requires `READ_MEDIA_AUDIO` permission
- **Themes:** 7 EVA-inspired + 8 original Ialemus themes preserved

**Not yet:** MVP 1B album/artist views, NAS Bridge, acquisition tools.

### Scan behavior

1. **Choose Music Folder** — SAF `OpenDocumentTree`; URI permission persisted in Room/DataStore flow
2. **Scan Selected Folders** — scans only user-approved SAF folders (audio MIME/extensions, 30s min duration)
3. **Full Device Music Scan** — explicit opt-in MediaStore scan; never runs on first launch

Fresh install does **not** auto-scan all device music.

### Default theme

**EVA-01 Berserk** (`ThemeId.EVA_01_BERSERK`) — mecha-inspired violet/green/orange HUD (original styling, no copyrighted assets).

---

| Permission | Purpose |
|------------|---------|
| `READ_MEDIA_AUDIO` | Local music scan (API 33+) |
| `READ_EXTERNAL_STORAGE` | Local music scan (API 32 and below, `maxSdkVersion=32`) |
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Background playback service |
| `POST_NOTIFICATIONS` | Playback notification (API 33+, optional) |
| `INTERNET` | Reserved for future bridge (unused in 1A playback) |

---

## Development

### Open the project

```bash
# Android Studio: File → Open → /home/heathen/Projects/ialemus
android-studio /home/heathen/Projects/ialemus
```

Ensure `local.properties` exists with your SDK path (gitignored):

```properties
sdk.dir=/home/heathen/Android/Sdk
```

JDK 17+ is required. Use Android Studio's bundled JBR (Java 21) or install `jdk17-openjdk`:

```bash
export JAVA_HOME=/opt/android-studio/jbr
# or: export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

### Build debug APK

```bash
cd /home/heathen/Projects/ialemus
export JAVA_HOME=/opt/android-studio/jbr
./gradlew assembleDebug
```

### Debug APK output path

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Basic Gradle checks

```bash
./gradlew tasks          # list available tasks
./gradlew assembleDebug  # build debug APK
./gradlew lintDebug      # run Android lint (optional)
./gradlew clean          # remove build outputs
```

Install on a connected device or HiBy R4:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Home-screen widget

Add the **Ialemus HUD playback module** widget from the Android widget picker. Tap to open the app. Shows last/current track title and artist when playback is active.

**Widget limitations (MVP 1B):** transport controls are placeholders (open app only); no play/pause/prev/next from widget yet; semi-transparent EVA-01 styling is fixed.

---

## Documentation

| Document | Description |
|----------|-------------|
| [IALEMUS_PROJECT_SPEC.md](docs/IALEMUS_PROJECT_SPEC.md) | Full product specification |
| [IALEMUS_ARCHITECTURE.md](docs/IALEMUS_ARCHITECTURE.md) | System architecture overview |
| [DECISIONS.md](docs/DECISIONS.md) | Architecture decision record |
| [NAS_BRIDGE_SPEC.md](docs/NAS_BRIDGE_SPEC.md) | Ialemus Bridge API and safety rules |
| [ANDROID_APP_SPEC.md](docs/ANDROID_APP_SPEC.md) | Android app modules and layouts |
| [IALEMUS_MVP_ROADMAP.md](docs/IALEMUS_MVP_ROADMAP.md) | MVP phase summary |
| [TODO.md](docs/TODO.md) | Task checklist by MVP phase |

**Configuration:** Copy [.env.example](.env.example) to `.env` on the NAS with local values. Never commit secrets.

---

## Architecture (summary)

```text
Android App  ──HTTPS/LAN + Bearer token──►  Ialemus Bridge  ──allowlisted profiles──►  NAS services
     │                                              │
     │  Playback, UI, Room library                  │  spot-dl, MeTube, slskd, watcher, rescan
     └──────────────────────────────────────────────┘
```

---

## Themes

**Default:** EVA-01 Berserk (EVA-inspired HUD palette)

EVA-inspired: EVA-01 Berserk · EVA-00 Prototype · EVA-02 Asuka Red · EVA-03 Shadow · EVA-05 Mass Production · Terminal Dogma · Tactical Command

Original Ialemus: Ghost in the Code · Terminal Kittie · Chthonic Signal · Neon Ossuary · Archive Black · Fallout CRT · Cyber Shrine · Candy Malware
