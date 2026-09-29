# EditForge AI — Android Studio Application

EditForge AI is an AI-powered music editing, mastering, and audio deliverable platform for producers, artists, DJs, and content creators. Built with modern Kotlin and Jetpack Compose for Android.

## Core Features Preserved & Ported to Android

- **Master Audio Ingestion & DSP Analysis:**
  - High-resolution master audio upload and preset selection.
  - Multi-phase DSP audio analysis engine: transient peak detection, tempo (BPM) extraction, harmonic key profiling, and loudness calculation (-14 LUFS integrated target).
  - Musical boundary detection: Intro, Verse, Chorus, Bridge, and Outro segment mapping.

- **Interactive Audio Waveform Visualizer:**
  - Real peak visualization with playback scrub-to-seek support.
  - Color-coded structure section overlays.
  - Real-time time elapsed and total duration tracking.

- **Deliverables Forge:**
  - Core Bundle Forge (1-click unlock for 60s radio, 30s hook, 15s social, and Sting).
  - Single deliverable and stem separation generators (Vocals, Drums, Bass, Other).
  - Alternative Variation generation (seed-based variation cuts for 60s, 30s, and 15s edits).

- **Audio Cut-Down Showcase Demos:**
  - Side-by-side comparison of real audio master transformations (60s, 30s, and 15s).
  - Dedicated interactive waveform players and detailed musical edit rationales.

- **Credits & Billing Engine:**
  - Spendable credits tracking and monthly subscription plan selector (Free, Plus, Pro, Premier).
  - Deliverables rate card (Full mix: 1cr, Edits: 2cr, Stems: 3cr, Alt cuts: 1cr, Core bundle: 5cr).
  - Complete credit transaction ledger with timestamped debits, credits, and remaining balance.

- **Audio Retention & Settings:**
  - 30-Day Master Audio Retention policy tracking with auto-purge warnings.
  - Audio engine specifications and complete artist content ownership guarantee.

- **Local Persistence with Room Database:**
  - Offline-first local data persistence using Room Database (`EditForgeDatabase`).
  - Pre-seeded studio demo tracks and responsive UI flows using Kotlin Coroutines and StateFlow.

## Tech Stack & Architecture

- **Platform:** Android (Min SDK 26, Target SDK 35)
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material Design 3 (Dark Studio Theme)
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Persistence:** Room Database (`ProjectDao`, `AnalysisDao`, `ExportDao`, `CreditDao`)
- **Concurrency:** Kotlin Coroutines & StateFlow
- **Theme:** EditForge Studio Dark theme (`ForgeNeonRed`, `ForgeElectricAmber`, `ForgeWaveformViolet`)
