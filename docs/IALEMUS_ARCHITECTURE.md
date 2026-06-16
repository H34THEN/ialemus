# Ialemus Architecture

High-level system design for Ialemus. Detailed decisions live in `DECISIONS.md`; API contracts in `NAS_BRIDGE_SPEC.md`; app modules in `ANDROID_APP_SPEC.md`.

---

## Core rule

**Android app → authenticated Ialemus Bridge API → Ugreen NAS Docker/media services**

The Android app must **not** execute raw shell commands, SSH root sessions, or arbitrary Docker commands. NAS-side operations use a restricted, allowlisted bridge. See ADR-001 in `DECISIONS.md`.

---

## System diagram

```text
┌─────────────────────────────────────────────────────────────────┐
│ Android Device (HiBy R4 / Phone)                                 │
│                                                                  │
│  Ialemus App                                                     │
│  ┌─────────────┐ ┌──────────────┐ ┌─────────┐ ┌────────────────┐ │
│  │ Player core │ │ Library      │ │ Acquire │ │ Settings +     │ │
│  │ Media3      │ │ scanner/Room │ │ Downloads│ │ encrypted     │ │
│  │ MediaSession│ │              │ │ screens  │ │ tokens        │ │
│  └─────────────┘ └──────────────┘ └─────────┘ └────────────────┘ │
│  ┌─────────────┐ ┌──────────────┐ ┌─────────────────────────┐ │
│  │ Themes      │ │ Shuffle +    │ │ Widget                  │ │
│  │ Lyrics HUD  │ │ favorites    │ │                         │ │
│  └─────────────┘ └──────────────┘ └─────────────────────────┘ │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │  HTTPS or HTTP over LAN
                             │  Authorization: Bearer <token>
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ Ugreen NAS                                                       │
│                                                                  │
│  ialemus-bridge (Ialemus Bridge)                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Token auth · allowlisted profiles · job queue · watcher   │   │
│  │ GET /health · POST/GET /jobs · POST /library/rescan     │   │
│  │ GET /library/recent · GET /library/browse                 │   │
│  └──────────────────────────────────────────────────────────┘   │
│         │              │              │              │           │
│         ▼              ▼              ▼              ▼           │
│     spot-dl         MeTube          slskd        folder paths    │
│     (container)     (yt-dlp)     (Soulseek)    (SMB/local)      │
└────────────────────────────┬────────────────────────────────────┘
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ Music storage                                                    │
│  NAS_MUSIC_ROOT · NAS_SPOTDL_DOWNLOADS · NAS_METUBE_DOWNLOADS   │
│  NAS_SLSKD_DOWNLOADS (see .env.example)                          │
└─────────────────────────────────────────────────────────────────┘
```

---

## Responsibility split

| Layer | Owns |
|-------|------|
| **Android app** | UI, playback, local Room cache, favorites, play counts, shuffle, themes, widget, bridge HTTP client |
| **Ialemus Bridge** | Auth, job queue, allowlisted spot-dl/MeTube/slskd execution, folder watch, library index, rescan |
| **NAS services** | Actual downloads and file storage (existing Docker/stack) |

---

## Android stack (summary)

Kotlin · Jetpack Compose · Media3/ExoPlayer · MediaSessionService · Room · DataStore · WorkManager · Retrofit or Ktor · Coil · encrypted settings storage

Sideload/APK friendly; Android 12+; no GMS required for core playback. Full stack in `DECISIONS.md` ADR-002.

---

## Safety boundaries

- No arbitrary commands from app
- Token auth on every bridge request
- Secrets in `.env` on NAS and encrypted app storage only
- Low-privilege bridge execution; redacted logs
- LAN-first; do not expose bridge to public internet without hardening

Details: `NAS_BRIDGE_SPEC.md` section 4.

---

## Related documents

- `DECISIONS.md` — ADRs including bridge rationale and DAP distribution
- `NAS_BRIDGE_SPEC.md` — endpoint contracts and service integrations
- `ANDROID_APP_SPEC.md` — screens, modules, HiBy R4 layout rules
- `IALEMUS_MVP_ROADMAP.md` / `TODO.md` — phased delivery
