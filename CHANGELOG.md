# Changelog

All notable changes to this project will be documented in this file.

## [2.0.0] - 2026-09-18

### Added
- **Force-Directed Knowledge Graph Engine (`ForceDirectedGraphEngine`):** Pure Kotlin Spring-Electrical physics simulation using Coulomb repulsion ($\vec{F}_{\text{rep}} \propto \frac{k_r^2}{d^2}$) and Hooke's elastic attraction ($\vec{F}_{\text{attr}} \propto \frac{(d - l_0)^2}{k_s}$) with kinetic temperature annealing.
- **Interactive Bioluminescent Mind Palace Canvas:** Interactive dragging, pinning, and pinch-to-zoom for knowledge nodes with progress-scaled radii and instant artifact inspection sheets.
- **Mathematical Reading Pacing Engine (`ReadingPacingEngine`):**
  - Text virtual pagination algorithm computing geometric character/line budgets for seamless `HorizontalPager` page flipping.
  - Adaptive Exponential Moving Average (EMA) reading speed estimator ($\text{EMA}_t = \alpha \cdot \text{WPM}_t + (1 - \alpha) \cdot \text{EMA}_{t-1}$) calculating dynamic minutes-to-finish.
  - Bionic Saccadic Reading mode bolding optical fixation points (35-45% of word length) for accelerated focus.
  - Circadian solar color temperature adaptation (Dawn Mist, Rivendell Daylight, Golden Twilight, Obsidian Midnight).
- **Multi-Factor Recommendation Engine (`RecommendationEngine`):** Ebbinghaus recency decay and readiness scoring replacing mock cards with dynamic resonance clusters (*Current Resonance*, *Forgotten Lore*, *New Discovery*).
- **Cognitive Chronicle Analytics Suite:** Real-time focus resonance scoring, reading duration tracking, velocity metrics, and consecutive day streaks.
- **Five-Realm Sanctuary Navigation:** Unification of bottom navigation (`Sanctuary`, `Corridors`, `Mind Palace`, `Chronicle`, `Settings`).
- **Data Layer Schema v3:** Room database migration adding reading sessions table (`reading_sessions`), bookmarking/note annotations, and atomic progress persistence.
- **Unit Test Suite:** Automated unit tests covering all mathematical models (`MathematicalEnginesTest`).

### Changed
- Refactored `ReaderScreen` to support dual paginated/continuous reading modes, font size scaling, in-scroll bookmarks, and one-tap distraction-free Focus Mode.
- Overhauled `HallOfImladrisScreen` to surface live algorithmic recommendations directly above active scrolls.
- Overhauled `AnalyticsScreen` with dual insight grids (Resonance, Indexed Scrolls, Minutes in Flow, Day Streak).

### Fixed
- Fixed unrouted `KnowledgeGraphScreen` and eliminated non-deterministic random coordinate positioning.
- Resolved static placeholder text in `RecommendationCluster`.
- Fixed missing reading progress persistence in Room DB.

## [1.0.0] - 2024-04-14

### Added
- Initial release of IMLADRIS: The Knowledge Sanctuary.
- Spatial UI with Hall of Imladris and Floating Artifacts.
- Custom Canvas-based Knowledge Graph for thematic visualization.
- Ethereal Reader with Focus Mode and Serif typography.
- Intelligent Notification System for Memory Recall.
- Jetpack Glance Home Screen Widgets (Continue Reading & Memory Recall).
- Library Scanner with SAF (System Access Framework) recursion.
- Analytics Suite with Focus Score and Reading Timeline.

### Changed
- Refined UI transitions and added haptic feedback for gateways.
- Standardized project structure for Clean Architecture.

### Security
- Offline-first implementation ensuring absolute data privacy.
