# IMLADRIS

**An ethereal, offline-first Android library and knowledge sanctuary.**

![Imladris Feature Graphic](docs/assets/feature_graphic.png)

---

IMLADRIS is not a file manager. It is a **Mind Palace**: an intelligent space that treats every document as an artifact of knowledge, every folder as a glowing gateway, and the act of reading as a journey worth remembering.

Inspired by the architecture of Rivendell, IMLADRIS brings together glassmorphic design, spatial navigation, and adaptive intelligence into a single, calm, majestic experience.

---

## Architecture

```
Presentation (Jetpack Compose)
        |
   Domain Layer
  (Models, UseCases)
        |
    Data Layer
 (Room DB, SAF Scanner)
        |
  Local Database + Storage
```

**Stack:** Kotlin · Jetpack Compose · Jetpack Glance · Hilt · Room · WorkManager · Clean Architecture

**Minimum SDK:** 26 (Android 8.0) · **Target SDK:** 34

---

## Features

**Spatial Navigation**
Navigate your library through luminous Gateways, each folder a portal to a themed collection of knowledge. Books float as Artifacts within each realm.

**Knowledge Graph**
A canvas-rendered node-link diagram that maps connections between your artifacts. Nodes pulse larger as you progress through a book. Supports pinch-to-zoom and pan gestures.

**Ethereal Reader**
A distraction-free reading environment built for long sessions. Playfair Display serif typography, adaptive brightness, and a Focus Mode that fades all UI chrome to reveal only text.

**Memory Recall**
The sanctuary remembers what you have read. Calm, non-intrusive WorkManager-scheduled notifications resurface highlights at your preferred reading hour, never more than twice a day.

**Reading Analytics**
Track focus scores, reading streaks, and a chronological timeline of your intellectual journey.

**Home Screen Widgets**
Jetpack Glance-powered widgets for immediately resuming your current book or reflecting on a recalled highlight, without opening the app.

---

## Project Structure

```
com.imladris
├── core
│   ├── data           # Room DB, SAF Library Scanner, Repository implementations
│   ├── domain         # Sealed models, Repository interfaces
│   ├── notifications  # WorkManager workers, Notification channels
│   ├── ui             # Design system, Glassmorphic components, Spatial engine
│   └── di             # Hilt modules and Widget entry points
├── feature
│   ├── hall           # Dashboard: The Hall of Imladris
│   ├── library        # Spatial folder browser
│   ├── graph          # Knowledge Graph canvas
│   ├── reader         # Ethereal reading experience
│   ├── analytics      # Focus scores, timelines
│   └── widgets        # Glance home screen widgets
└── ui.navigation      # Type-safe Compose NavGraph
```

---

## Getting Started

**Prerequisites:** Android Studio Iguana+, JDK 17, Android SDK 34

```bash
# Clone the repository
git clone https://github.com/<username>/imladris.git

# Open in Android Studio, let Gradle sync, then:
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/Imladris-v1.0.apk
```

On first launch, tap the **+** button to select your root library folder. IMLADRIS will recursively scan and populate your sanctuary.

---

## Visual Identity

| App Icon | Palette |
|---|---|
| ![App Icon](docs/assets/app_icon.png) | **Midnight** `#0A0E14` · **Silver** `#E1E8ED` · **Gold** `#D4AF37` · **Teal** `#64FFDA` |

A full icon specification (adaptive layers, monochrome, notification glyph) is available in [`docs/icon_specs.md`](docs/icon_specs.md).

---

## License

MIT License. See [LICENSE](LICENSE) for details.

---

*"Deep roots are not reached by the frost."*
