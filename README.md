# Planify

Planify is a planning app for Android focusing on simplicity. Instead of a big task manager, it asks a single question every day — **what are your
plans for today?** — then reminds you about them until you're done. Anything
left unfinished is quietly left in a searchable history.

## Key Features

#### Daily plans, not endless backlogs
- Each day starts with a prompt to write down that day's plans as free-form text.
- Plans can be turned into a checklist by prefixing lines with a marker (`--` by
  default, customizable in Settings) — tap a line to check it off.
- Plans can be added and edited throughout the day.

#### Smart reminders
- A local notification goes out shortly after you set your plans, and repeats on
  a cooldown you control (default: every 2 hours) for as long as unfinished
  plans remain.
- At night, plans for the day automatically reset.

#### Plan ahead
- A built-in date picker lets you queue up plans for future days, not just today.
- A day-selector drawer/sidebar lists days you've prepared, so you can jump between them or remove ones you no longer need.

#### History
- Every day's plans are automatically archived once they're finalized, so you can scroll back and see what you had planned (and finished) on past days. Each entry can be deleted individually.

#### Notes
- A separate space for freeform notes, independent of the daily
  plans — for things that aren't tied to a specific day.

#### Account & sync
- Optional Google Sign-In lets you sync
  plans, notes, and history to the cloud across devices.
- The app works fully offline/locally without signing in — sync is opt-in and
  gracefully falls back when there's no connection.

#### Self-updating
- Updates can be downloaded and installed directly from within the app, with a
  live download-progress indicator. A changelog dialog summarizes what's new after each update.

#### Personalization
- Light/dark theme toggle.
- Adjustable reminder frequency and independent toggles for plan reminders vs.
  the end-of-day reset notification.
- Custom checklist prefix/marker, with an in-app explainer showing how it works.
- Adaptive layout: a bottom navigation bar on phones, a navigation rail on
  tablets/large screens.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3, edge-to-edge layout, adaptive
  navigation (bottom bar / navigation rail) via `WindowSizeClass`
- **Architecture:** MVVM (`ViewModel` + `StateFlow` per screen), a shared
  `Repository` backed by `SharedPreferences` for local storage
- **Cloud:** Firebase Firestore (sync) + Firebase Analytics, Google Sign-In via
  Credential Manager
- **Scheduling:** `AlarmManager` exact alarms + `BroadcastReceiver`s for daily
  reminders, resets, and boot-time rescheduling
- **Min SDK / Target SDK:** 26 / 36 (compiled against SDK 37)

## Localization

Ships with English and Russian string resources.

A `local.properties` file with a `GITHUB_TOKEN` entry is required for the
in-app update checker to query the GitHub Releases API at build time.
