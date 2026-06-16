# Spotify Integration Plan (Ialemus)

MVP 1B.4 adds a **scaffold only** — no raw Spotify streaming through Ialemus ExoPlayer.

## Architecture

| Layer | Role |
|-------|------|
| **LOCAL CORE** | Ialemus ExoPlayer / Media3 — SAF & MediaStore files only |
| **SPOTIFY REMOTE** | External Spotify app / Spotify Connect / App Remote — separate playback |

**Do not:**
- Stream Spotify URLs through ExoPlayer
- Download Spotify audio
- Merge Spotify tracks into the local Ialemus queue as playable audio items

**Do:**
- Spotify Web API (Authorization Code + **PKCE**) for account, playlists, search metadata
- Spotify Android SDK **App Remote** (future) for play/pause/next/previous on the installed Spotify app
- Open Spotify app / `spotify:` URI fallback

## Developer setup

1. Create an app at [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
2. Note **Client ID** (not a secret — user configures in Ialemus Settings)
3. Add **Redirect URI**: `com.heathen.ialemus://spotify-callback` (or custom, must match Settings)
4. Android package name: `com.heathen.ialemus`
5. **Do not** embed Client Secret in the Android app

## Auth (Web API)

- Flow: Authorization Code with PKCE
- Scopes (initial): `user-read-private`, `user-read-email`, `playlist-read-private`, `user-read-playback-state`
- Token storage: **TODO** — EncryptedSharedPreferences or Android Keystore before production
- Token refresh: implement only when secure storage is in place

## Playback (App Remote)

- Dependency: Spotify Android SDK (App Remote module) — not added in MVP 1B.4 scaffold
- Requires Spotify app installed on device
- Controls map to Spotify app state, not `PlayerViewModel` local queue

## UI surfaces

- **Streaming tab** — account, remote now playing scaffold, icon controls, Open Spotify
- **Settings → Spotify Integration** — Client ID, Redirect URI, login/logout scaffold
- **Downloads → spotDL Module** — future Bridge handoff from Spotify playlist URLs

## Premium / policy

- Respect Spotify Terms of Service and Premium requirements for playback features
- spotDL execution remains **Bridge-only** on NAS — never from Android

## Next implementation steps

1. Add PKCE code verifier/challenge generation + custom tab / WebView auth callback handler
2. Register intent filter for redirect URI in `AndroidManifest.xml`
3. Store access/refresh tokens securely
4. Add Spotify App Remote dependency + connection lifecycle
5. Display Spotify now playing metadata from Web API / App Remote
6. Optional: “Send playlist to Bridge” when Bridge MVP 2 ships
