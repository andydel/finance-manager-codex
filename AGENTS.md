# Repository Guidelines

## Project Structure & Module Organization
- `app/` holds the Android application module; Compose screens live under `ui/` by feature (e.g., `ui/home`, `ui/settings`), shared state is wired through `AppContainer` and `FinanceManagerApplication`.
- Domain rules stay in `domain/` while persistence is isolated in `data/` with Room DAOs and repositories; keep cross-layer models in `domain/model` to avoid leaking entity details.
- Resources belong in `app/src/main/res`; prefer feature-scoped subdirectories (`values`, `drawable`, `raw`) and keep sample fixtures or design docs out of source control.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` compiles the debug variant and is the baseline sanity build before opening a PR.
- `./gradlew testDebugUnitTest` runs JVM unit tests; pair it with `./gradlew lint` when touching UI or resources to catch style regressions.
- `./gradlew connectedDebugAndroidTest` exercises instrumented Compose tests on a device/emulator; run it for navigation or persistence changes.

## Coding Style & Naming Conventions
- Kotlin sources use 4-space indentation, trailing commas for multiline constructs, and `camelCase` for functions/variables; classes, ViewModels, and Composables stay in `PascalCase` with a clear suffix (`MainActivity`, `FinanceSummaryScreen`).
- Resource identifiers follow Android conventions: `snake_case` for layout or drawable names and `lowerCamelCase` for string keys; keep Compose previews annotated and grouped in the same file as the composable.
- Prefer immutable state flows, constructor injection via `AppContainer`, and one ViewModel per screen to align with existing architecture.

## Testing Guidelines
- Co-locate unit tests in `app/src/test/java` mirroring the package under test; use JUnit4 and Mockito/KotlinX coroutines test helpers already on the classpath.
- Instrumented or Compose UI tests live in `app/src/androidTest/java`; name them `<Feature>UiTest` and focus on happy path plus one guard-rail scenario.
- Target meaningful coverage rather than a number: new repositories require unit tests, while UI changes should include at least one screenshot or `composeTestRule` assertion.

## Commit & Pull Request Guidelines
- Follow the existing history by writing short, imperative summaries (e.g., `Add savings summary view`) and include scoped bodies if more context is needed.
- Start every new feature or sizable fix on a dedicated `feature/<your-topic>` branch, then open a PR for review before merging.
- Group related changes per commit, reference issue IDs in the footer when applicable, and avoid mixing refactors with feature work.
- PRs need a concise changelog, screenshots for UI updates, and a checklist of manual or automated test runs so reviewers can reproduce results quickly.

## Configuration Tips
- Keep API keys or environment overrides in `local.properties`; never commit sample secrets—document placeholders inside the README instead.
- When modifying Gradle plugins or Room schemas, update the `build/` artifacts only through Gradle and clear stale caches with `./gradlew clean` before pushing if builds become inconsistent.
