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

### MVP 1C / remaining 1B polish (next)

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
