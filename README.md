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

## License

This project is available under the MIT license.
