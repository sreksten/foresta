# foresta-android

Il prototipo Android della Foresta (Fase 7 di `porting_libGDX.md`): un progetto Gradle **fuori da Maven**, quindi senza numero di livello, che mette insieme il motore e l'interfaccia libGDX in un'applicazione Android.

## Cosa fa e perché

`AndroidLauncher` è il gemello di `MainLibgdx` (desktop): crea l'`EsecutoreGdx` (il thread di rendering è il thread principale), collega il motore con `AvvioDelMotore` e avvia `ForestaLibGDX` con la disposizione `VERTICALE` (la barra delle icone sul lato destro). Il layout lo sceglie `ForestaLibGDX` in `create()` dalla misura vera dello schermo: la superficie su cui libGDX disegna (con il modo immersivo, lo schermo senza barre di sistema e senza notch) e la densità (`Gdx.graphics.getDensity()`); se lo schermo in dp contiene il layout di sempre è quello del tablet, altrimenti quello del telefono (`SceltaDelLayout`, `layout_telefono.md` §2 e §3.3). Nel log c'è la riga `Schermo …: layout …`. L'attività è forzata in orizzontale nel manifest (`sensorLandscape`): sul telefono l'utente deve ruotare il dispositivo. Il gioco salva e scrive il log in `<user.home>/.foresta`: su Android `user.home` si punta alla cartella privata dell'app (`getFilesDir()`) prima di avviare il motore. `NotificaUscitaDalGioco` chiude l'applicazione con `Gdx.app.exit()`.

## Dipendenze

Non sono moduli Maven: i moduli del gioco (`12-foresta-motore`, `12-foresta-libgdx`, e da loro tutto il resto) si prendono dal repository Maven locale (`~/.m2`, gruppo `com.threeamigos`), e libGDX (`gdx-backend-android` con le librerie native per le quattro architetture) da Maven Central. Quindi **prima di compilare** si installano i moduli: `mvn install -DskipTests -pl 12-foresta-motore,12-foresta-libgdx -am` dalla radice. `mavenLocal()` è limitato al gruppo `com.threeamigos`: il repository locale ha i pom di libGDX (per il desktop) ma non i jar delle librerie native Android, e Gradle si fermerebbe lì.

## Pagine di prova su Android

Per vedere un pezzo dell'interfaccia senza il motore (e fare screenshot con `adb exec-out screencap -p`): `adb shell am start -n com.threeamigos.foresta/.android.AndroidLauncher --es pagina 12` apre la pagina di prova numero 12 (la mappa), la stessa di `MainLibgdx PAGINE_DI_PROVA -Dforesta.pagina=12` sul desktop (i numeri sono in `ForestaGdx`). Le pagine di prova sono 1142×656: le misure dello schermo di gioco vero (area di 1022×732) sono diverse.

## Come si compila e si prova

Servono un JDK 17 (l'Android Gradle Plugin non vuole il 22), l'SDK Android (piattaforma 36, build-tools) e `adb`; come si sono installati e come si tolgono sta in `risorse_e_documenti_vari/android_disinstalla.sh`.

```
export JAVA_HOME=<JDK 17> ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
cd foresta-android && ./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.threeamigos.foresta -c android.intent.category.LAUNCHER 1
adb logcat --pid=$(adb shell pidof com.threeamigos.foresta)
```

Versioni: Gradle 8.13, AGP 8.13.0, `compileSdk` 36 (libGDX 1.14 usa AndroidX e `androidx.core` 1.17 lo richiede), `targetSdk` 34, `minSdk` 24 (Android 7.0). `AndroidX` è abilitato in `gradle.properties`.

## L'icona

È la «F» del titolo (`LogoForesta.png`, pixel art 39×62, in `foresta-android/icone-android/` insieme allo script che la trasforma) su fondo pergamena (#DEBD94), come icona adattiva (`mipmap-anydpi-v26`: sfondo a tinta unita e primo piano trasparente). `icone-android/genera_icone.py` ingrandisce la bitmap **senza interpolare** con un fattore intero (×1 mdpi, ×2 xhdpi, ×3 xxhdpi, ×4 xxxhdpi), sempre dentro l'area di sicurezza di 66 su 108 punti, e scrive i PNG in `app/src/main/res`. Per cambiarla: si sostituisce la bitmap (o il colore nello script) e si rilancia `python3 icone-android/genera_icone.py` (serve Pillow). Niente SVG: la pixel art con retinatura non si vettorializza senza perdere l'aspetto.

## Stato (2026-11-06)

Provato su un **Realme Pad (RMP2103)**: un tablet da 10,4″, 2000×1200, Android 11, MediaTek G80 / Mali-G52, 3 GB di RAM. Il gioco parte, gira a circa 60 fotogrammi al secondo e mostra l'intro con la barra delle icone verticale. Il tocco non è stato provato sistematicamente.

## Osservazioni

- **`minSdk` 24, verificato il 2026-11-11.** Lint (`./gradlew lintDebug`) non trova errori, ma guarda solo i sorgenti dell'app (`AndroidLauncher`), non il bytecode dei jar del gioco; quello si è controllato a parte, confrontando ogni riferimento a classi, metodi e campi dei 30 jar con `platforms/android-36/data/api-versions.xml`. Sopra la 24 escono solo cose che D8 sistema da sé, come conferma `dexdump` sull'APK: `String.join` (26) diventa un backport (`$$ExternalSyntheticBackport`), le lambda (`LambdaMetafactory`, `MethodHandles`) sono desugarate, e `ByteBuffer.clear()` con il tipo di ritorno covariante di Java 9 (in `TexturaAggiornabileGdx`: i moduli si compilano con `source`/`target` 1.8 ma senza `--release 8`) diventa `Buffer.clear()`. Sotto la 24 non si scende: gli stream e le interfacce funzionali (`java.util.function`) vengono dalla 24, e `androidx.core` di libGDX 1.14 vuole comunque almeno la 21.
- Nei moduli non-Swing non ci sono import di `java.awt`, `java.time`, `java.nio.file` né `javax.*`: il compilatore Android li ha accettati tutti.
- **Precisione delle coordinate delle texture**: lo shader predefinito di libGDX è `mediump`, che sulle GPU Mali è a 16 bit e al bordo delle immagini degli atlanti fa leggere la riga rossa di separazione; per questo si usa `SpriteBatchDiPrecisioneAltaGdx` (vedi `12-foresta-libgdx.md`).
- Il Realme Pad è un tablet, non un telefono: non dice niente sul layout da telefono (rapporto 1,67 contro circa 2,2). Con `adb shell wm size` si può far fingere allo schermo una forma più stretta.

## Da fare / idee aperte

- Provare il tocco e una partita intera (salvataggio, caricamento, inventario, mappa, combattimento, tastiera di sistema).
- La firma dell'app e il pacchetto per lo store (Fase 7, punto 5), le icone, il nome e i margini di sicurezza (notch); lo schermo sempre acceso c'è già (`useWakelock` in `AndroidLauncher`).
- Il layout per telefono (Fase 6): fatto, da provare sul Realme Pad (deve restare sul layout del tablet: lo dice la riga `Schermo …` del log, e dipende dalla densità che il tablet dichiara) e con `adb shell wm size` / `wm density` per fingere un telefono.
