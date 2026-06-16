# Ialemus Architecture

The preferred architecture is:

Android app -> authenticated Ialemus Bridge API -> Ugreen NAS Docker/media services

The Android app should not execute raw shell commands directly. NAS-side commands should be handled through a restricted, authenticated, allowlisted bridge.
