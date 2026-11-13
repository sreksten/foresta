# foresta-android

Il prototipo Android della Foresta (Fase 7 di `porting_libGDX.md`): un progetto Gradle, quindi senza numero di livello, che mette insieme il motore e l'interfaccia libGDX in un'applicazione Android. Non è un modulo Maven, ma Maven lo lancia: `mvn package -Pandroid` dalla radice scrive anche l'APK di debug (modulo [`13-foresta-app-android`](13-foresta-app-android.md)).

## Cosa fa e perché

`AndroidLauncher` è il gemello di `MainLibgdx` (desktop): crea l'`EsecutoreGdx` (il thread di rendering è il thread principale), collega il motore con `AvvioDelMotore` e avvia `ForestaLibGDX` con la posizione e lo stile (normale o Dock; senza scelta il Dock) della barra salvati nelle opzioni (se il giocatore non l'ha mai scelta: a destra sul telefono, in basso sul tablet se ci sta; `layout_telefono.md` §7.1). Il layout lo sceglie `ForestaLibGDX` in `create()` dalla misura vera dello schermo: la superficie su cui libGDX disegna (con il modo immersivo, lo schermo senza barre di sistema e senza notch) e la densità (`Gdx.graphics.getDensity()`); se lo schermo in dp contiene il layout di sempre è quello del tablet, altrimenti quello del telefono (`SceltaDelLayout`, `layout_telefono.md` §2 e §3.3). Nel log c'è la riga `Schermo …: layout …`. L'attività è forzata in orizzontale nel manifest (`sensorLandscape`): sul telefono l'utente deve ruotare il dispositivo. Il gioco salva e scrive il log in `<user.home>/.foresta`: su Android `user.home` si punta alla cartella privata dell'app (`getFilesDir()`) prima di avviare il motore. `NotificaUscitaDalGioco` chiude l'applicazione con `Gdx.app.exit()`.

## Dipendenze

I jar del gioco (`12-foresta-motore`, `12-foresta-libgdx` e tutto quello che sta sotto, 30 in tutto) li copia Maven in `13-foresta-app-android/target/libs` e Gradle li prende da lì (`fileTree`, cartella data con `-Pforesta.libs`; senza, quella di prima): non passano più da `~/.m2`, quindi Gradle usa quelli appena compilati e non serve `mvn install`. libGDX (`gdx-backend-android` con le librerie native per le quattro architetture) viene da Maven Central; Maven non lo copia, perché due copie di `gdx` darebbero classi doppie.

## Pagine di prova su Android

Per vedere un pezzo dell'interfaccia senza il motore (e fare screenshot con `adb exec-out screencap -p`): `adb shell am start -n com.threeamigos.foresta/.android.AndroidLauncher --es pagina 12` apre la pagina di prova numero 12 (la mappa), la stessa di `MainLibgdx PAGINE_DI_PROVA -Dforesta.pagina=12` sul desktop (i numeri sono in `ForestaGdx`). Le pagine di prova sono 1142×656: le misure dello schermo di gioco vero (area di 1022×732) sono diverse.

## Come si compila e si prova

Servono un JDK dal 17 al 21 (l'Android Gradle Plugin non vuole il 22), l'SDK Android (piattaforma 36, build-tools) e `adb`; come si sono installati e come si tolgono sta in `risorse_e_documenti_vari/android_disinstalla.sh`.

```
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
mvn package -Pandroid -DskipTests -Dandroid.java.home=<JDK 17>     # dalla radice: jar e foresta-android-0.0.1-SNAPSHOT.apk
adb install -r foresta-android-0.0.1-SNAPSHOT.apk
adb shell monkey -p com.threeamigos.foresta -c android.intent.category.LAUNCHER 1
adb logcat --pid=$(adb shell pidof com.threeamigos.foresta)
```

Versioni: Gradle 8.13, AGP 8.13.0, `compileSdk` 36 (libGDX 1.14 usa AndroidX e `androidx.core` 1.17 lo richiede), `targetSdk` 34, `minSdk` 24 (Android 7.0). `AndroidX` è abilitato in `gradle.properties`.

## L'icona

È la «F» del titolo (`LogoForesta.png`, pixel art 39×62, in `foresta-android/icone-android/` insieme allo script che la trasforma) su fondo pergamena (#DEBD94), come icona adattiva (`mipmap-anydpi-v26`: sfondo a tinta unita e primo piano trasparente). `icone-android/genera_icone.py` ingrandisce la bitmap **senza interpolare** con un fattore intero (×1 mdpi, ×2 xhdpi, ×3 xxhdpi, ×4 xxxhdpi), sempre dentro l'area di sicurezza di 66 su 108 punti, e scrive i PNG in `app/src/main/res`. Per cambiarla: si sostituisce la bitmap (o il colore nello script) e si rilancia `python3 icone-android/genera_icone.py` (serve Pillow). Niente SVG: la pixel art con retinatura non si vettorializza senza perdere l'aspetto. L'icona adattiva c'è solo da Android 8 (API 26): per le versioni prima (il `minSdk` è 24; il Moto G con Android 7.1 mostrava l'icona generica) lo script scrive anche le icone tradizionali, `ic_launcher.png` (la F su un quadrato pergamena con gli angoli arrotondati) e `ic_launcher_round.png` (su un cerchio), in xhdpi (96 pixel, F ×1), xxhdpi (144, ×2) e xxxhdpi (192, ×2); la mdpi la ricava il sistema dalla xhdpi, perché nemmeno la F ×1 ci sta in 48 pixel.

## Stato (2026-11-12)

Provato su due dispositivi:

- **Realme Pad (RMP2103)** (2026-11-06): un tablet da 10,4″, 2000×1200, Android 11, MediaTek G80 / Mali-G52, 3 GB di RAM. Il gioco parte, gira a circa 60 fotogrammi al secondo e mostra l'intro con la barra delle icone verticale.
- **Motorola Moto G 2014 LTE (XT1072, «thea»)** (2026-11-11): un telefono da 5″, 1280×720, Snapdragon 400 / Adreno 305, 1 GB di RAM (870 MB disponibili al sistema), con **LineageOS 14.1, Android 7.1.2 (API 25)**: la prima prova sotto Android 8, cioè del `minSdk` 24. Il gioco parte e mostra l'intro con il testo che scorre e la barra delle icone verticale; nel log nessun errore di API mancanti (`NoSuchMethodError`, `NoClassDefFoundError`, `VerifyError`). In memoria occupa circa 67 MB (PSS; 24 MB di heap Java, 23 MB di grafica). Il testo si legge bene. Dal 2026-11-12 usa il layout del telefono (`layout_telefono.md`): niente più fascia nera a destra, la pila dei riquadri che scorre di lato, le cornici dell'inventario strette; ci si è giocata una partita con il dito (locande, acquisti, inventario, combattimenti), e da lì sono venute le correzioni di quei giorni (il riquadro di testo meno sfumato, gli sprite sotto i riquadri che li coprono, il cartiglio che restava dopo un tocco, la regola del pianista). L'icona generica al posto della F, su Android 7.1, è corretta con le icone tradizionali (vedi sopra). Il Moto G esce di fabbrica con Android 6.0 (API 23): per arrivare alla 7.1 si è sbloccato il bootloader (codice dal portale Motorola), si è installato TWRP 3.7.0 (`fastboot flash recovery`) e da lì la nightly ufficiale `lineage-14.1-20180224-nightly-thea-signed.zip` (copia su archive.org, `adb sideload`).

Sul Realme Pad non si sono provati sistematicamente il tocco né una partita intera, e non si è ancora visto se resta sul layout del tablet.

## Osservazioni

- **`minSdk` 24, verificato il 2026-11-11.** Lint (`./gradlew lintDebug`) non trova errori, ma guarda solo i sorgenti dell'app (`AndroidLauncher`), non il bytecode dei jar del gioco; quello si è controllato a parte, confrontando ogni riferimento a classi, metodi e campi dei 30 jar con `platforms/android-36/data/api-versions.xml`. Sopra la 24 escono solo cose che D8 sistema da sé, come conferma `dexdump` sull'APK: `String.join` (26) diventa un backport (`$$ExternalSyntheticBackport`), le lambda (`LambdaMetafactory`, `MethodHandles`) sono desugarate, e `ByteBuffer.clear()` con il tipo di ritorno covariante di Java 9 (in `TexturaAggiornabileGdx`: i moduli si compilano con `source`/`target` 1.8 ma senza `--release 8`) diventa `Buffer.clear()`. Sotto la 24 non si scende: gli stream e le interfacce funzionali (`java.util.function`) vengono dalla 24, e `androidx.core` di libGDX 1.14 vuole comunque almeno la 21.
- Nei moduli non-Swing non ci sono import di `java.awt`, `java.time`, `java.nio.file` né `javax.*`: il compilatore Android li ha accettati tutti.
- **Precisione delle coordinate delle texture**: lo shader predefinito di libGDX è `mediump`, che sulle GPU Mali è a 16 bit e al bordo delle immagini degli atlanti fa leggere la riga rossa di separazione; per questo si usa `SpriteBatchDiPrecisioneAltaGdx` (vedi `12-foresta-libgdx.md`).
- Il Realme Pad è un tablet, non un telefono: non dice niente sul layout da telefono (rapporto 1,67 contro circa 2,2). Con `adb shell wm size` si può far fingere allo schermo una forma più stretta.

## Da fare / idee aperte

- Sul Moto G: il salvataggio e il caricamento, la tastiera di sistema con il campo di testo aperto, le botteghe e le tattiche con le cornici strette. Sul Realme Pad il tocco e una partita intera.
- La firma dell'app e il pacchetto per lo store (Fase 7, punto 5), il nome e i margini di sicurezza (notch); lo schermo sempre acceso c'è già (`useWakelock` in `AndroidLauncher`).
- Il layout per telefono (Fase 6): fatto e provato sul Moto G; da provare sul Realme Pad (deve restare sul layout del tablet: lo dice la riga `Schermo …` del log, e dipende dalla densità che il tablet dichiara).
