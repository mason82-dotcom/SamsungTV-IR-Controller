# Samsung TV IR Remote – OnePlus 13R / Android 16

Native Android-Fernbedienung für ältere Samsung-TVs (ca. 2010, klassische Samsung/AA59-Infrarotcodes).

## Zielgerät
- OnePlus 13R
- Android 16
- `compileSdk 36`
- `targetSdk 36`
- `minSdk 23`
- Consumer-IR über Android `ConsumerIrManager`
- Trägerfrequenz: 38 kHz

## Funktionen
Power, Source, Mute, Menu, Info, Guide, Tools, Exit, Lautstärke +/−, Kanal +/−,
Navigation + OK/Return, Ziffern 0–9, Farbtasten und Mediensteuerung.

Die App benötigt kein Internet, kein Konto und keine Standortberechtigung. Im Manifest ist ausschließlich
die für Consumer-IR erforderliche Berechtigung `android.permission.TRANSMIT_IR` eingetragen.
Beim Start prüft die App, ob Android einen IR-Sender meldet.

## Android-16-Anpassungen
- API 36 als Compile- und Target-SDK
- Android Gradle Plugin 8.13.2
- Gradle 8.13
- Java 17
- Edge-to-edge Insets berücksichtigt

## APK bauen
### GitHub Actions
Unter **Actions → Build Android APK → Run workflow** starten.
Das Artefakt heißt `SamsungTVIRRemote-debug-apk`.

### Lokal
Mit Android SDK Platform 36, JDK 17 und Gradle 8.13:

```bash
gradle :app:assembleDebug
```

APK danach:
`app/build/outputs/apk/debug/app-debug.apk`
