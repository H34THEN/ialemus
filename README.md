# Ialemus

Android music and media player for HiBy R4 DAP, Android phones, and self-hosted NAS media workflows.

Ialemus connects to a Ugreen NAS media stack through **Ialemus Bridge** — a restricted, authenticated API on the NAS. The Android app never runs raw shell, SSH, or arbitrary Docker commands. Acquisition (spot-dl, MeTube, slskd/Soulseek), folder watching, and library rescans go through allowlisted bridge endpoints.

**Features (planned):** local and NAS music, play counts, favorites, strong shuffle, lyrics, themes, widgets, portrait/landscape layouts, and polished DAP low-power mode.

**Status:** MVP 1A EVA HUD Pass — tap-to-play fixed, folder-first SAF scan, EVA-01 default theme, full HUD interface overhaul. Debug APK buildable (`0.2.2-mvp1a-eva-hud`).

### MVP 1A EVA HUD Pass (current)

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
