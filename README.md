# HEADSHOT AI PRO — Android AI Training Agent

**HEADSHOT AI PRO** is a standalone native Android training application engineered with **Kotlin**, **Jetpack Compose (Material 3)**, **Clean Architecture + MVVM**, **Kotlin Coroutines & StateFlow**, and **Room Local Database**.

It helps Free Fire MAX players improve aiming accuracy, upward drag-headshot timing, first-shot reaction speed, and hardware-specific sensitivity configuration without modifying or interacting with the game client.

## Architecture & Module Layout

```text
HeadshotAIPro/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/headshotaipro/trainer/
        │   │   ├── HeadshotAiApplication.kt
        │   │   ├── MainActivity.kt
        │   │   ├── domain/
        │   │   │   ├── model/
        │   │   │   │   ├── TrainingMode.kt
        │   │   │   │   ├── ShotMetrics.kt
        │   │   │   │   ├── SensitivityModels.kt
        │   │   │   │   └── PerformanceAnalytics.kt
        │   │   │   ├── repository/
        │   │   │   │   └── TrainingRepository.kt
        │   │   │   └── usecase/
        │   │   │       ├── AnalyzeSensitivityUseCase.kt
        │   │   │       ├── EvaluateShotPhysicsUseCase.kt
        │   │   │       └── GetPerformanceAnalyticsUseCase.kt
        │   │   ├── data/
        │   │   │   ├── local/
        │   │   │   │   ├── TrainingSessionEntity.kt
        │   │   │   │   ├── TrainingSessionDao.kt
        │   │   │   │   └── HeadshotDatabase.kt
        │   │   │   └── repository/
        │   │   │       └── TrainingRepositoryImpl.kt
        │   │   └── presentation/
        │   │       ├── HeadshotAiApp.kt
        │   │       ├── theme/
        │   │       │   ├── Color.kt
        │   │       │   ├── Type.kt
        │   │       │   └── Theme.kt
        │   │       ├── viewmodel/
        │   │       │   ├── TrainingViewModel.kt
        │   │       │   ├── SensitivityViewModel.kt
        │   │       │   └── AnalyticsViewModel.kt
        │   │       └── screens/
        │   │           ├── HeadshotArenaScreen.kt
        │   │           ├── SensitivityAnalyzerScreen.kt
        │   │           └── PerformanceAnalyticsScreen.kt
        │   └── res/
        │       ├── drawable/ic_launcher_foreground.xml
        │       ├── values/strings.xml
        │       ├── values/themes.xml
        │       └── xml/ (backup_rules.xml, data_extraction_rules.xml)
        └── test/java/com/headshotaipro/trainer/domain/
            └── HeadshotAiDomainTest.kt
```

## Security & Fair-Play Compliance

- **100% Standalone Application**: Does not read/write game memory, inject code, overlay on top of Free Fire / Free Fire MAX, or interact with anti-cheat systems.
- **Minimal Permissions**: Uses only `android.permission.VIBRATE` for optional haptic feedback and stores training logs locally via AndroidX Room (`headshot_ai_pro.db`).

## Building the Debug APK Locally

1. Open the `HeadshotAIPro` directory in **Android Studio Ladybug (2024.2.1)+** or **Koala (2024.1.1)+** with **JDK 17** and **Android SDK 34**.
2. Sync Gradle (`./gradlew tasks`) or click **Run 'app'** (`Shift + F10`).
3. To assemble the debug APK from the terminal:
   ```bash
   ./gradlew assembleDebug
   ```
   The generated APK will be output to `app/build/outputs/apk/debug/app-debug.apk`.
4. To execute unit tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```
