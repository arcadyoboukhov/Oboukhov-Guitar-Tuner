## Oboukhov Guitar Tuner

Android guitar tuner application built with Kotlin and Jetpack Compose.  
The app uses microphone input and pitch detection to help tune instruments.

## Features

- Real-time pitch detection from device microphone (`RECORD_AUDIO` permission required)
- Note and frequency mapping for a wide pitch range
- Jetpack Compose UI
- Portrait-oriented app experience

## Tech Stack

- Kotlin
- Android SDK (`minSdk 24`, `targetSdk 34`, `compileSdk 34`)
- Jetpack Compose + Material 3
- Tarsos DSP (included as local `.aar`)

## Requirements

- Android Studio (recent version with AGP 8.7+ support)
- JDK 17 (recommended for current Android Gradle Plugin versions)
- Android SDK 34 installed

## Getting Started

1. Clone or download this repository.
2. Open the project folder in Android Studio.
3. Let Gradle sync finish.
4. Build and run on a physical Android device or emulator.
5. Grant microphone permission when prompted.

## Build From Command Line

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

On macOS/Linux:

```bash
./gradlew assembleDebug
```

APK output is typically generated under:

`app/build/outputs/apk/debug/`

## Project Structure

- `app/src/main/java/com/example/tuneralpha/` - Main application source
- `app/src/main/assets/json/` - Preset data files
- `app/libs/` - Local third-party binaries (including Tarsos DSP `.aar`)
- `gradle/libs.versions.toml` - Dependency and plugin versions

## Permissions

- `android.permission.RECORD_AUDIO` - Required for pitch detection from microphone input

## Proprietary License

Copyright (c) 2026 Oboukhov.

This project is proprietary and confidential. All rights reserved.

No part of this repository may be copied, modified, distributed, published, sublicensed, sold, or used in derivative works without prior written permission from the copyright owner.

Access to this source code does not grant any license to use the software except for private evaluation by authorized parties.

## Disclaimer

This software is provided "as is" without warranties of any kind, express or implied.

