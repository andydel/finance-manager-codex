# Finance Manager

Finance Manager is an Android application built with Jetpack Compose and Room that helps users track balances across multiple accounts, review transactions, and view an overall wealth summary.

## Features

- **Tabbed dashboard** separating Current, Savings & Investments, and Debt accounts.
- **Account management** for creating accounts with starting balances and currencies.
- **Transaction entry** that supports expenses and income with category selection.
- **Wealth summary** showing totals for current, savings, investments, and debt balances.
- **Profile settings** to manage the default user name and base currency.

## Tech Stack

- Kotlin + Jetpack Compose (Material3)
- Navigation Compose
- Room persistence with Kotlin Flows
- MVVM architecture with ViewModels and state holders

## Getting Started

1. Ensure you have Android Studio Flamingo (or newer) with the Android 14 SDK installed.
2. Clone the repository and open it in Android Studio.
3. Allow the IDE to sync Gradle; then use `./gradlew assembleDebug` or the Run configuration to build/install.

## Contributing

Review `AGENTS.md` for repository guidelines covering project structure, build commands, coding style, and pull request expectations before opening a change.

## Project Structure

```
app/
  src/main/java/com/andydel/financemanager/
    data/        # Room entities, DAOs, and repository
    domain/      # Domain models
    ui/          # Compose UI, navigation, and ViewModels
  src/main/res/  # Resources (themes, strings, icons)
```

## Testing

Unit tests can be run with:

```
./gradlew testDebugUnitTest
```

(Instrumented tests are not yet implemented.)

## Run Generator

  - Bring it up with the testing profile so only that container starts: docker compose --profile testing up -d payroll-data-generator.
  - Follow its log stream to confirm messages are flowing: docker compose logs -f payroll-data-generator.
  - When finished, stop it with docker compose --profile testing stop payroll-data-generator (add rm to drop the container if you want a clean rerun).

## License

This project is available under the MIT license.
