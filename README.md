# Ialemus

Android music and media player for HiBy R4 DAP, Android phones, and self-hosted NAS media workflows.

Ialemus connects to a Ugreen NAS media stack through **Ialemus Bridge** — a restricted, authenticated API on the NAS. The Android app never runs raw shell, SSH, or arbitrary Docker commands. Acquisition (spot-dl, MeTube, slskd/Soulseek), folder watching, and library rescans go through allowlisted bridge endpoints.

**Features (planned):** local and NAS music, play counts, favorites, strong shuffle, lyrics, themes, widgets, portrait/landscape layouts, and polished DAP low-power mode.

**Status:** MVP 0 — Android scaffold initialized (Kotlin + Jetpack Compose). Debug APK buildable.

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

Ghost in the Code · Terminal Kittie · Chthonic Signal · Neon Ossuary · Archive Black · Fallout CRT · Cyber Shrine · Candy Malware
