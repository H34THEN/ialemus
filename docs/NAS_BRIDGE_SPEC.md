# Ialemus Bridge — NAS Service Specification

**Service name:** `ialemus-bridge`  
**Role:** Authenticated, allowlisted API broker on the Ugreen NAS  
**Consumers:** Ialemus Android app (HiBy R4, phones)

The Android app **never** executes shell, SSH, or arbitrary Docker commands. All NAS-side acquisition and indexing flows through this bridge.

See also: `DECISIONS.md` (ADR-001), `IALEMUS_PROJECT_SPEC.md` (sections 5, 17–18).

---

## 1. Purpose

Ialemus Bridge provides a safe HTTP API between the mobile app and self-hosted NAS media services:

| Responsibility | Owner |
|----------------|-------|
| Token-authenticated API | Bridge |
| Allowlisted job profiles | Bridge |
| spot-dl / MeTube / slskd invocation | Bridge |
| Job queue and status | Bridge |
| Download folder watching | Bridge |
| Library index and rescan | Bridge |
| Playback and UI | Android app |
| Local favorites / play counts | Android app |

---

## 2. Deployment model

- **Host:** Ugreen NAS (same LAN as DAP/phone, optional VPN URL for remote)
- **Packaging:** Docker Compose service recommended
- **Network:** LAN-only by default; do not expose bridge to the public internet without TLS and strict firewall rules
- **Configuration:** Environment variables (see `.env.example` at repo root — never commit real `.env`)

Suggested base URL pattern:

```text
http://<nas-host>:<IALEMUS_BRIDGE_PORT>/api
```

All paths below are relative to `/api`.

---

## 3. Authentication

Every request requires:

```http
Authorization: Bearer <IALEMUS_BRIDGE_TOKEN>
```

Rules:

- Tokens are generated on the NAS and entered manually in the app settings.
- Tokens are **not** embedded in the APK.
- Rotate tokens periodically; support multiple active tokens if needed later.
- Invalid or missing token → `401 Unauthorized`.
- Bridge logs must **redact** tokens and third-party API keys.

---

## 4. Safety rules

### 4.1 No arbitrary commands

- No freeform shell input from the app or API clients.
- No SSH root sessions triggered by app requests.
- No arbitrary Docker `exec` strings composed from user text.
- Only **pre-defined command profiles** with validated parameters may run.

### 4.2 Allowlisted command profiles

Profiles are defined in bridge configuration (YAML/JSON), not in the app:

```yaml
profiles:
  spotdl_music_m4a_lyrics:
    service: spotdl
    label: "spot-dl M4A + lyrics"
    allowed_options:
      - skipExisting
      - outputTarget
    command_template: "<placeholder — match real NAS container>"
```

Each profile declares:

- `service` — `spotdl`, `metube`, `slskd`, or `generic` (still allowlisted)
- Allowed option keys and value enums
- Output path roots from environment variables only
- Low-privilege execution user inside container or host

### 4.3 Input validation

- URLs must be parsed, scheme-checked, and escaped before substitution.
- File paths for browse/rescan must be confined to configured music roots (no path traversal).
- Search strings and profile names must match allowlists.

### 4.4 Secrets and logging

- No secrets committed to git (`.env` is gitignored; use `.env.example`).
- Bridge runs with minimum required filesystem and Docker permissions.
- Job logs redact tokens, passwords, and API keys.
- App offers “copy redacted diagnostics” — bridge should support a redacted log export mode.

### 4.5 Privilege

- Bridge process should not run as NAS root.
- Container exec uses service-specific accounts where possible.
- Write access limited to configured download and music library paths.

---

## 5. API endpoints (placeholder contract)

All endpoints require Bearer auth unless noted.

### 5.1 Health

```http
GET /health
```

**Response `200`:**

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

Service states: `ok`, `degraded`, `unavailable`, `disabled`.

---

### 5.2 Create job

```http
POST /jobs
```

**Request body:**

```json
{
  "service": "spotdl",
  "profile": "spotdl_music_m4a_lyrics",
  "input": "https://open.spotify.com/playlist/example",
  "options": {
    "skipExisting": true,
    "outputTarget": "nas_music"
  }
}
```

| Field | Notes |
|-------|-------|
| `service` | `spotdl`, `metube`, `slskd` |
| `profile` | Must match allowlisted profile name |
| `input` | URL or service-specific search token |
| `options` | Only keys allowed by profile |

**Response `201`:**

```json
{
  "id": "job_01HXYZ",
  "status": "queued",
  "createdAt": "2026-06-16T12:00:00Z"
}
```

---

### 5.3 List jobs

```http
GET /jobs?status=running&service=spotdl&limit=50
```

**Response `200`:**

```json
{
  "jobs": [
    {
      "id": "job_01HXYZ",
      "service": "spotdl",
      "status": "running",
      "input": "https://open.spotify.com/playlist/example",
      "profile": "spotdl_music_m4a_lyrics",
      "progressPercent": 42.5,
      "createdAt": "2026-06-16T12:00:00Z",
      "completedAt": null,
      "errorMessage": null
    }
  ]
}
```

---

### 5.4 Get job

```http
GET /jobs/{id}
```

**Response `200`:** Single job object (same shape as list item, plus optional `logTail` redacted).

**Response `404`:** Unknown job id.

---

### 5.5 Cancel job

```http
POST /jobs/{id}/cancel
```

**Response `200`:**

```json
{
  "id": "job_01HXYZ",
  "status": "cancelled"
}
```

Cancellation is best-effort for already-running external processes.

---

### 5.6 Library rescan

```http
POST /library/rescan
```

**Optional body:**

```json
{
  "paths": ["${NAS_MUSIC_ROOT}", "${NAS_SPOTDL_DOWNLOADS}"]
}
```

If `paths` omitted, rescan all configured watch roots from environment.

**Response `202`:**

```json
{
  "scanId": "scan_01HXYZ",
  "status": "started"
}
```

---

### 5.7 Recently added library items

```http
GET /library/recent?limit=100
```

**Response `200`:**

```json
{
  "items": [
    {
      "id": "trk_abc123",
      "title": "Example Track",
      "artist": "Example Artist",
      "album": "Example Album",
      "durationMs": 240000,
      "path": "/volume1/Music/Example/example.m4a",
      "origin": "spotdl",
      "dateAdded": "2026-06-16T11:30:00Z",
      "artworkUrl": null
    }
  ]
}
```

---

### 5.8 Browse library path

```http
GET /library/browse?path=/volume1/Music
```

`path` must be under an allowed root. Reject `..` and paths outside configured roots → `400`.

**Response `200`:**

```json
{
  "path": "/volume1/Music",
  "entries": [
    { "name": "Artist Name", "type": "directory" },
    { "name": "album.m3u", "type": "file", "sizeBytes": 1024 }
  ]
}
```

---

## 6. Service integrations

### 6.1 spot-dl

**Bridge role:** Queue download jobs using allowlisted `spotdl` profiles.

**Typical flow:**

1. App sends `POST /jobs` with `service: "spotdl"`.
2. Bridge validates URL and profile.
3. Bridge runs pre-approved container command (placeholder):

   ```bash
   # Placeholder — actual container name and mounts must match NAS setup
   docker exec <spotdl-container> spotdl download "<url>" \
     --output "<NAS_SPOTDL_DOWNLOADS>/{artist}/{album}/{title}.{ext}" \
     --format m4a \
     --skip-existing
   ```

4. Watcher detects new files under `NAS_SPOTDL_DOWNLOADS` or configured music root.
5. Bridge index updates; app polls `GET /library/recent` or receives SSE.

**App must not:** Run `spotdl` or `docker exec` on device.

---

### 6.2 MeTube

**Bridge role:** Submit download requests to MeTube (yt-dlp-backed) via allowlisted profile or MeTube HTTP API proxy.

**Typical flow:**

1. App sends `POST /jobs` with `service: "metube"`.
2. Bridge forwards to MeTube internal URL with validated options (audio-only, format, output folder).
3. Completed files land under `NAS_METUBE_DOWNLOADS`.
4. Watcher + rescan expose new media to app.

**Options (validated enums):** `audioOnly`, `format`, `outputTarget`.

---

### 6.3 slskd / Soulseek

**Bridge role:** Proxy search and download requests to slskd API using server-stored credentials; never expose slskd admin key to the app binary.

**Typical flow:**

1. Search: `POST /jobs` with `service: "slskd"` and search profile, or dedicated search endpoint (future).
2. Bridge calls slskd REST API on LAN.
3. Downloads complete under `NAS_SLSKD_DOWNLOADS`.
4. App shows queue status via `GET /jobs` and imports completed files.

**Safety:**

- slskd API key lives in bridge `.env` only.
- App stores bridge token only.
- Optional fallback: deep link to slskd web UI for advanced users.

---

## 7. Folder watcher

Watch configured paths (from environment):

| Variable | Purpose |
|----------|---------|
| `NAS_MUSIC_ROOT` | Primary music library |
| `NAS_SPOTDL_DOWNLOADS` | spot-dl output |
| `NAS_METUBE_DOWNLOADS` | MeTube output |
| `NAS_SLSKD_DOWNLOADS` | slskd completed downloads |

**Behavior:**

1. Detect new/changed files (debounce until size stable).
2. Extract metadata (ffprobe/tag reader).
3. Update bridge library index.
4. Emit event via poll-friendly `GET /library/recent` or optional SSE/WebSocket channel.

---

## 8. Error model

| HTTP | Meaning |
|------|---------|
| 400 | Invalid input, unknown profile, path traversal |
| 401 | Missing or invalid token |
| 404 | Job or path not found |
| 409 | Job already terminal |
| 500 | Internal error (message safe for client; details in redacted server log) |
| 503 | Dependency down (spotdl, MeTube, slskd) |

Example error body:

```json
{
  "error": "invalid_profile",
  "message": "Profile 'freeform_shell' is not allowlisted."
}
```

---

## 9. Optional future endpoints

Not required for MVP 2–3; document for planning only:

- `GET /events` — SSE job and watcher events
- `GET /profiles` — List allowlisted profile names and labels for app UI
- `POST /library/sync-stats` — Optional play-count sync from app

---

## 10. Implementation notes

**Suggested runtimes:** Node.js/Fastify, Python/FastAPI, or Go — decide in open questions (`TODO.md`).

**MVP bridge deliverables:**

1. Docker Compose service `ialemus-bridge`
2. Token auth middleware
3. Profile registry from config file
4. Job store (SQLite or JSON file initially)
5. Health checks for downstream services
6. Folder watcher + minimal library index
7. Placeholder command templates wired to real NAS service names when known
