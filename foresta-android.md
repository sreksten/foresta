# foresta-android

Il prototipo Android della Foresta (Fase 7 di `porting_libGDX.md`): un progetto Gradle **fuori da Maven**, quindi senza numero di livello, che mette insieme il motore e l'interfaccia libGDX in un'applicazione Android.

## Cosa fa e perché

`AndroidLauncher` è il gemello di `MainLibgdx` (desktop): crea l'`EsecutoreGdx` (il thread di rendering è il thread principale), collega il motore con `AvvioDelMotore` e avvia `ForestaLibGDX` con la disposizione `VERTICALE` (la barra delle icone sul lato destro). L'attività è forzata in orizzontale nel manifest (`sensorLandscape`): sul telefono l'utente deve ruotare il dispositivo. Il gioco salva e scrive il log in `<user.home>/.foresta`: su Android `user.home` si punta alla cartella privata dell'app (`getFilesDir()`) prima di avviare il motore. `NotificaUscitaDalGioco` chiude l'applicazione con `Gdx.app.exit()`.

## Dipendenze

Non sono moduli Maven: i moduli del gioco (`12-foresta-motore`, `12-foresta-libgdx`, e da loro tutto il resto) si prendono dal repository Maven locale (`~/.m2`, gruppo `com.threeamigos`), e libGDX (`gdx-backend-android` con le librerie native per le quattro architetture) da Maven Central. Quindi **prima di compilare** si installano i moduli: `mvn install -DskipTests -pl 12-foresta-motore,12-foresta-libgdx -am` dalla radice. `mavenLocal()` è limitato al gruppo `com.threeamigos`: il repository locale ha i pom di libGDX (per il desktop) ma non i jar delle librerie native Android, e Gradle si fermerebbe lì.

## Come si compila e si prova

Servono un JDK 17 (l'Android Gradle Plugin non vuole il 22), l'SDK Android (piattaforma 36, build-tools) e `adb`; come si sono installati e come si tolgono sta in `risorse_e_documenti_vari/android_disinstalla.sh`.

```
export JAVA_HOME=<JDK 17> ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
cd foresta-android && ./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.threeamigos.foresta -c android.intent.category.LAUNCHER 1
adb logcat --pid=$(adb shell pidof com.threeamigos.foresta)
```

Versioni: Gradle 8.13, AGP 8.13.0, `compileSdk` 36 (libGDX 1.14 usa AndroidX e `androidx.core` 1.17 lo richiede), `targetSdk` 34, `minSdk` 26 (il piano diceva 24: per il prototipo si evitano i problemi con le API Java 8 di libreria come `String.join`; si torna a 24 dopo una verifica con lint). `AndroidX` è abilitato in `gradle.properties`.

## Stato (2026-11-06)

Provato su un **Realme Pad (RMP2103)**: un tablet da 10,4″, 2000×1200, Android 11, MediaTek G80 / Mali-G52, 3 GB di RAM. Il gioco parte, gira a circa 60 fotogrammi al secondo e mostra l'intro con la barra delle icone verticale. Il tocco non è stato provato sistematicamente.

## Osservazioni

- Nei moduli non-Swing non ci sono import di `java.awt`, `java.time`, `java.nio.file` né `javax.*` (945 file): il compilatore Android li ha accettati tutti.
- **Precisione delle coordinate delle texture**: lo shader predefinito di libGDX è `mediump`, che sulle GPU Mali è a 16 bit e al bordo delle immagini degli atlanti fa leggere la riga rossa di separazione; per questo si usa `SpriteBatchDiPrecisioneAltaGdx` (vedi `12-foresta-libgdx.md`).
- Il Realme Pad è un tablet, non un telefono: non dice niente sul layout da telefono (rapporto 1,67 contro circa 2,2). Con `adb shell wm size` si può far fingere allo schermo una forma più stretta.

## Da fare / idee aperte

- Provare il tocco e una partita intera (salvataggio, caricamento, inventario, mappa, combattimento, tastiera di sistema).
- Riportare `minSdk` a 24 verificando con lint le API usate.
- La firma dell'app e il pacchetto per lo store (Fase 7, punto 5), le icone, il nome, schermo sempre acceso e margini di sicurezza (notch).
- Il layout per telefono (Fase 6).
