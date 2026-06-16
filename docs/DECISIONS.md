# Ialemus — Architecture Decision Record

This document records key architecture and technology decisions for Ialemus. Decisions here should stay consistent with `IALEMUS_PROJECT_SPEC.md`, `IALEMUS_ARCHITECTURE.md`, `NAS_BRIDGE_SPEC.md`, and `ANDROID_APP_SPEC.md`.

---

## ADR-001: Android app does not execute shell, SSH, or Docker commands

**Status:** Accepted  
**Date:** 2026-06-16

### Context

Ialemus needs to trigger NAS-side media acquisition workflows (spot-dl, MeTube, slskd/Soulseek), watch download folders, and rescan libraries. A naive approach would have the Android app SSH into the Ugreen NAS as root and run arbitrary Docker or shell commands.

That pattern is unsafe on a mobile client:

- APKs can be reverse-engineered; embedded credentials or command templates leak.
- Arbitrary command execution from a phone creates a remote-code-execution surface on the NAS.
- Android sandboxing and background limits make long-running shell sessions unreliable.
- HiBy R4 and phone users should not need root shells or Termux hacks for core features.

### Decision

The Android app **must not** directly run:

- Raw shell commands
- SSH root sessions
- Arbitrary Docker commands
- Freeform command strings from user input

All NAS-side media acquisition and indexing operations go through a restricted, authenticated NAS-side service called **Ialemus Bridge** (`ialemus-bridge`).

### Architecture

```text
Android App  --HTTPS/LAN + Bearer token-->  Ialemus Bridge  --allowlisted profiles-->  NAS services
```

The app calls typed HTTP API endpoints. The bridge maps requests to pre-approved command profiles and service integrations. See `NAS_BRIDGE_SPEC.md` for endpoint and safety details.

### Consequences

- Bridge must be deployed and maintained on the NAS (Docker Compose recommended).
- App settings store bridge URL and API token only — not NAS root passwords.
- New acquisition features require bridge-side allowlist updates, not app-side shell access.
- Job status, folder watching, and library rescans are bridge responsibilities.

---

## ADR-002: Android technology stack

**Status:** Accepted  
**Date:** 2026-06-16

### Decision

Build the Android client with:

| Layer | Choice |
|-------|--------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Playback | AndroidX Media3 / ExoPlayer |
| Background playback | `MediaSessionService` |
| Local database | Room |
| Preferences | DataStore Preferences |
| Secrets / tokens | Encrypted settings storage (e.g. EncryptedSharedPreferences or Android Keystore-backed DataStore) |
| Background work | WorkManager |
| HTTP client | Retrofit + OkHttp **or** Ktor Client |
| Serialization | Kotlinx Serialization |
| Images / artwork | Coil |
| DI | Hilt or Koin (decide at project init) |
| Widget | Glance App Widget or classic RemoteViews |

### Rationale

- **Kotlin + Compose:** Modern Android defaults; good for portrait/landscape adaptive layouts and theming.
- **Media3 / ExoPlayer:** Standard for local and streaming playback; integrates with system media controls.
- **MediaSessionService:** Lock-screen, notification, Bluetooth, and widget control without custom hacks.
- **Room + DataStore:** Structured library metadata and user settings with clear separation.
- **WorkManager:** Reliable library scans, cache jobs, and bridge sync on Android 12+.
- **Retrofit/Ktor:** Typed bridge API client; no shell execution.
- **Coil:** Efficient album art loading from local URIs and NAS URLs.
- **Encrypted settings:** Bridge tokens and service keys must not live in plaintext.

### Consequences

- No Google Play Services required for core playback (important for HiBy R4 sideload use).
- Network and media permissions only as needed per Android version.
- Bridge API models should be shared or mirrored between app and bridge where practical.

---

## ADR-003: Sideload- and DAP-friendly distribution

**Status:** Accepted  
**Date:** 2026-06-16

### Decision

Ialemus remains **sideload/APK friendly** for HiBy R4 and manual phone installs:

- Target **Android 12+** (API 31+).
- Core playback and local library features work **without Google Play Services**.
- Release builds distributable via APK / GitHub Releases; Play Store is optional later.
- Provide **DAP low-power mode**: reduced animations, less polling, Wi-Fi-only bridge sync, OLED-friendly default theme (`Archive Black`).

### Rationale

HiBy R4 is an Android DAP often used without Google Mobile Services. Users expect APK sideloading, not Play Store dependency.

### Consequences

- Avoid Play Services–only APIs for playback, storage, or auth.
- FCM/push for job completion is optional; polling or LAN WebSocket/SSE via bridge is sufficient initially.
- Signing and release workflow documented before first APK publish.

---

## ADR-004: Bridge owns command execution; app owns playback state

**Status:** Accepted  
**Date:** 2026-06-16

### Decision

- **Ialemus Bridge:** Job queue, allowlisted spot-dl/MeTube/slskd profiles, folder watching, library index, health checks.
- **Android app:** UI, local/NAS playback, Room library cache, favorites/play counts, shuffle, themes, widget.

The app mirrors bridge library data locally for fast browsing and offline metadata; it does not re-implement acquisition tooling.

### Consequences

- Play counts and favorites are stored locally first; optional NAS sync is a later enhancement.
- Completed downloads appear in the app after bridge watcher + rescan + app import cycle.

---

## ADR-005: Theme system as first-class product feature

**Status:** Accepted  
**Date:** 2026-06-16

### Decision

Ship eight named themes at launch (stubs in MVP 0, polish through MVP 5):

1. Ghost in the Code
2. Terminal Kittie
3. Chthonic Signal
4. Neon Ossuary
5. Archive Black
6. Fallout CRT
7. Cyber Shrine
8. Candy Malware

Themes affect colors, typography accents, HUD density, and optional scanline effects. `Archive Black` is the recommended DAP battery default; `Ghost in the Code` or `Chthonic Signal` are candidates for phone default (see open questions in `TODO.md`).

---

## ADR-006: Documentation-first project bootstrap

**Status:** Accepted  
**Date:** 2026-06-16

### Decision

Complete planning documentation before generating Android Studio / Gradle / Kotlin source trees. MVP 0 delivers docs, scaffold *planning*, theme name registry, and architecture validation — not a generated app until explicitly approved.

### Consequences

- `TODO.md` tracks phased work without premature `app/` module creation.
- Bridge and app specs can evolve without merge conflicts in generated boilerplate.

---

## Related documents

| Document | Purpose |
|----------|---------|
| `IALEMUS_PROJECT_SPEC.md` | Full product specification |
| `IALEMUS_ARCHITECTURE.md` | High-level system diagram |
| `NAS_BRIDGE_SPEC.md` | Bridge API and safety rules |
| `ANDROID_APP_SPEC.md` | App modules and layout expectations |
| `IALEMUS_MVP_ROADMAP.md` | Phase summary |
| `TODO.md` | Actionable task checklist by MVP |
