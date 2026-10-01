# TV IR Controller – OnePlus 13R / Android 16

Native Android-Infrarot-Fernbedienung für:

- ältere Samsung-TVs mit klassischen Samsung32/AA59-Codes
- Philips Android TVs mit Chassis **QM16.3E** über **RC6 Mode 0**

## Zielgerät
- OnePlus 13R
- Android 16
- `compileSdk 36`
- `targetSdk 36`
- `minSdk 23`
- Android `ConsumerIrManager`

## Samsung-Profil
- Samsung32
- 38 kHz
- Power, Source, Mute, Menu, Info, Guide, Tools, Exit
- Lautstärke / Kanal
- Navigation / OK
- Ziffern 0–9
- Farbtasten
- Mediensteuerung

## Philips QM16.3E
- Philips RC6 Mode 0
- 36 kHz
- System/Adresse 0
- Power, Source, Mute, Menu, Info, Guide, Home, Exit
- Lautstärke / Kanal
- Navigation / OK / Zurück
- Ziffern 0–9
- Farbtasten
- Ambilight
- Rewind, Play, Pause, Fast Forward, Stop, Record

Die App merkt sich das zuletzt ausgewählte TV-Profil.

## Sicherheit / Berechtigungen
Die App benötigt kein Internet, kein Konto und keine Standortberechtigung.
Im Manifest ist nur `android.permission.TRANSMIT_IR` für den Consumer-IR-Sender eingetragen.

## Version
`1.2.0-android16`

## APK bauen
GitHub Actions erzeugt eine klar benannte APK:

`TV-IR-Controller_v1.2.0_Android16_Samsung-Philips-QM16.3E.apk`
