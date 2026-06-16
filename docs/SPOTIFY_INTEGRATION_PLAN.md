# Spotify Integration Plan (Ialemus)

MVP 1B.5 implements **Authorization Code + PKCE** login. Spotify playback remains **SPOTIFY REMOTE** — never raw audio through Ialemus ExoPlayer.

## Architecture

| Layer | Role |
|-------|------|
| **LOCAL CORE** | Ialemus ExoPlayer / Media3 — SAF & MediaStore files only |
| **SPOTIFY REMOTE** | Spotify Web API + Spotify app / Connect — separate playback |

**Do not:** stream Spotify through ExoPlayer, download Spotify audio, use Client Secret in Android.

## Personal-app defaults (MVP 1B.5)

| Setting | Value |
|---------|--------|
| Client ID | `9c6067114c44430fba5b6a627a907e61` (prefilled; override in Settings advanced) |
| Redirect URI | `ialemus://spotify-auth-callback` |
| Package | `com.heathen.ialemus` |
| SHA-1 | Required in Spotify Developer Dashboard (debug/release as needed) |

No Client Secret in the app. PKCE only.

## Auth flow

1. User taps **Connect Spotify** → PKCE verifier/challenge + state stored
2. Custom Tab opens Spotify authorize URL (`code_challenge_method=S256`)
3. Spotify redirects to `ialemus://spotify-auth-callback?code=...&state=...`
4. MainActivity validates state, exchanges code for tokens (no secret)
5. Profile + playback fetched via Web API

## Token storage (MVP)

- Tokens in separate DataStore (`ialemus_spotify_auth`)
- **TODO:** migrate to EncryptedSharedPreferences / Keystore
- Refresh token support implemented; session expiry shows relogin prompt

## Web API playback controls

- Play / pause / next / previous / shuffle via Spotify Web API
- Requires an **active Spotify device** (Spotify app open or Connect target)
- 204 = no active playback; 404 = no device — show “Open Spotify App”

## App Remote (next phase)

Spotify Android SDK App Remote not added in 1B.5. Web API controls need an active device; App Remote can provide richer in-app control when the Spotify app is installed.

## Premium / policy

Respect Spotify Terms of Service and Premium requirements. spotDL remains Bridge-only on NAS.
