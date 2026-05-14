# SatStack

A native Android Bitcoin DCA journal app built with Kotlin and Jetpack Compose.

---

## Features

- **Onboarding** — guided 4-page setup (currency, milestone goal, exchange URL, optional sample data)
- **Biometric Vault** — fingerprint or device credential authentication gate
- **Dashboard** — stack summary card, milestone progress bar, DCA transaction history with swipe-to-delete
- **Add DCA Entry** — modal bottom sheet with date picker, fiat amount, sats acquired, and implied price/BTC
- **Privacy Mode** — double-tap the stack card to hide/show balances
- **Analytics** — Fear & Greed Index (historical), BTC price in GBP & USD, portfolio P&L
- **Settings** — dark mode toggle, milestone goal, exchange URL, fiat currency selector (all ISO 4217)
- **Milestone Notifications** — push notification when your stack hits the goal
- **Offline Banner** — graceful degradation with a network-state broadcast receiver
- **Splash Screen** — Android 12+ native splash screen

---

## Tech Stack

| | |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM, StateFlow |
| Database | Room 2.6.1 |
| Preferences | DataStore |
| Auth | `androidx.biometric` |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 (Android 15) |

---

## APIs

- **Fear & Greed Index** — [alternative.me/fng](https://api.alternative.me/fng/?limit=31)
- **BTC Price** — [CoinGecko simple/price](https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=gbp,usd) (GBP & USD, 5-min auto-refresh)

All network calls use Android's built-in `java.net.URL` and `org.json`; no third-party HTTP client is required.

---

## Running the App

### Authentication

On first launch, the app requires biometric or PIN authentication to access your stack.

**Physical device:** Ensure a fingerprint or PIN is enrolled in device Settings before launching.

**Emulator:**
1. Go to Settings → Security → Fingerprint and enrol a fingerprint.
2. When the prompt appears, open Extended Controls (`...` in the emulator toolbar) → Fingerprint → Touch the Sensor.

**No fingerprint enrolled?** Tap **Use PIN / Password** — the app falls back to device credential automatically.
