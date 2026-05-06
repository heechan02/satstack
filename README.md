# Sat Stack

A native Android Bitcoin DCA journal app built with Kotlin and Jetpack Compose.

---

## Running the App

### Authentication

On first launch, the app requires biometric or PIN authentication to access your stack.

**If using a physical device:**
- Ensure a fingerprint or PIN is enrolled in device Settings before launching the app.

**If using an emulator:**
- Go to Settings → Security → Fingerprint and enrol a fingerprint before launching.
- When the fingerprint prompt appears, open Extended Controls (`...` in the emulator toolbar) → Fingerprint → Touch the Sensor.

**No fingerprint enrolled?**
- Tap **Use PIN / Password** on the prompt — the app falls back to device credential automatically. No fingerprint setup is required.
