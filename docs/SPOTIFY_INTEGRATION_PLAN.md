# Spotify Integration Plan (Ialemus)

MVP 1B.6 adds **Spotify App Remote** and **Web API Connect device helpers** on top of MVP 1B.5 PKCE login. Spotify playback remains **SPOTIFY REMOTE** — never raw audio through Ialemus ExoPlayer.

## Architecture

| Layer | Role |
|-------|------|
| **LOCAL CORE** | Ialemus ExoPlayer / Media3 — SAF & MediaStore files only |
| **SPOTIFY REMOTE** | Spotify app on HiBy R4 + App Remote + Spotify Connect Web API |

**Do not:** stream Spotify through ExoPlayer, download Spotify audio, use Client Secret in Android.

**Ialemus is not a Spotify Connect receiver device.** The HiBy R4 becomes the Spotify playback target when the **Spotify app** is installed, logged in, and playing on that device.

## Personal-app defaults

| Setting | Value |
|---------|--------|
| Client ID | `9c6067114c44430fba5b6a627a907e61` (prefilled; override in Settings advanced) |
| Redirect URI | `ialemus://spotify-auth-callback` |
| Spotify package | `com.spotify.music` |
| Package | `com.heathen.ialemus` |
| SHA-1 | Required in Spotify Developer Dashboard (debug/release as needed) |

No Client Secret in the app. PKCE only.

## Auth flow (Web API)

1. User taps **Connect Spotify** → PKCE verifier/challenge + state stored
2. Custom Tab opens Spotify authorize URL (`code_challenge_method=S256`)
3. Spotify redirects to `ialemus://spotify-auth-callback?code=...&state=...`
4. MainActivity validates state, exchanges code for tokens (no secret)
5. Profile + playback fetched via Web API; Connect devices listed via `/me/player/devices`

## App Remote (MVP 1B.6)

Dependency: local `app/libs/spotify-app-remote-release-0.8.0.aar` from [Spotify Android SDK releases](https://github.com/spotify/android-sdk/releases/tag/v0.8.0-appremote_v2.1.0-auth) (App Remote is not on Maven Central).

| Component | Role |
|-----------|------|
| `SpotifyAppDetector` | Package visibility for `com.spotify.music` |
| `SpotifyRemoteRepository` | Connect, subscribe to player state, play/pause/skip/play URI |
| `SpotifyViewModel` | Merges App Remote state with Web API playback |

**Connect flow:**

1. Install Spotify on HiBy R4 (sideload OK)
2. Log into Spotify app
3. Start any track in Spotify app
4. Open Ialemus → Streaming → **Connect Spotify Remote**
5. App Remote connects to Spotify app on same device; track metadata streams into UI

Redirect URI for App Remote: same `ialemus://spotify-auth-callback` registered in Spotify Developer Dashboard.

## Web API playback controls

- Play / pause / next / previous / shuffle via Spotify Web API
- `GET /me/player/devices` — list Connect devices (name, type, active, volume, restricted)
- `PUT /me/player` — transfer playback to selected device (`play: false` by default)
- Requires logged-in Web API session; active device often required for transport commands
- 204 = no active playback; 404 = no device — show “Open Spotify on this HiBy R4 and start playback”

**Control priority:** App Remote when connected → Web API when active Connect device exists → controls disabled with actionable message.

## Token storage (MVP)

- Tokens in separate DataStore (`ialemus_spotify_auth`)
- **TODO:** migrate to EncryptedSharedPreferences / Keystore
- Refresh token support implemented; session expiry shows relogin prompt

## Manual test checklist (HiBy R4)

1. Install APK: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
2. Ensure Spotify app installed on HiBy R4
3. Log into Spotify app
4. Start playing any track in Spotify app
5. Open Ialemus → Streaming tab
6. Confirm Spotify app installed status detected
7. Tap **Connect Spotify Remote**
8. Confirm connection or useful error
9. Confirm current track appears if connected
10. Try previous / play-pause / next
11. Tap **Refresh Devices** (requires Web API login)
12. Confirm “No active device” messaging when none appear
13. Confirm local playback and folder scan still work

## Known limitations

- App Remote may fail if Spotify app not logged in or not recently active
- Shuffle/repeat not exposed via App Remote SDK in this integration
- Repeat toggle on Web API not fully implemented
- Web API device list empty until Spotify app has been active at least once
- Premium may be required for some Web API transport endpoints
- spotDL remains Bridge-only on NAS

## Premium / policy

Respect Spotify Terms of Service and Premium requirements. spotDL remains Bridge-only on NAS.
