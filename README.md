# La Foresta
My favourite game! (Italian language only)

## Build

- `mvn package` dalla radice: compila, esegue i test e scrive nella radice i jar eseguibili (`foresta-0.0.1-SNAPSHOT-jar-with-dependencies.jar` per Swing, `foresta-libgdx-0.0.1-SNAPSHOT-jar-with-dependencies.jar` per libGDX).
- `mvn package -Pandroid` dalla radice: fa le stesse cose e in più costruisce l'APK di debug per Android, `foresta-android-0.0.1-SNAPSHOT.apk`, nella radice accanto ai jar (dettagli in `13-foresta-app-android.md` e `foresta-android.md`).

### Cosa serve sulla macchina per l'APK

- **Maven** (3.8 o successivo) e un **JDK** per compilare il gioco (va bene l'11).
- **Un JDK dal 17 al 21** per Gradle: l'Android Gradle Plugin non va sotto il 17 e non vuole il 22. Se Maven gira già con uno di questi non serve altro; altrimenti si indica con `-Dandroid.java.home`:
  ```
  mvn package -Pandroid -Dandroid.java.home=/usr/lib/jvm/java-21-openjdk-amd64
  ```
- **L'SDK Android** con la piattaforma 36 (`platforms;android-36`) e i build-tools, indicato dalla variabile `ANDROID_HOME` (oppure `sdk.dir=...` in `foresta-android/local.properties`). Con i command-line tools:
  ```
  sdkmanager --licenses
  sdkmanager "platforms;android-36" "build-tools;35.0.0" "platform-tools"
  ```
  Se i build-tools non sono della versione che vuole l'Android Gradle Plugin, Gradle li scarica da sé (per questo vanno accettate le licenze).
  Senza l'SDK Gradle si ferma con «SDK location not found».
- **Gradle non va installato**: lo scarica il wrapper `foresta-android/gradlew` (8.13), che la prima volta scarica anche l'Android Gradle Plugin e libGDX. Quindi la prima volta serve la **connessione a Internet**.
- Per provare l'APK su un dispositivo: **`adb`** (in `platform-tools`) e `adb install -r foresta-android-0.0.1-SNAPSHOT.apk`. L'APK è firmato con la chiave di debug: si installa con adb o direttamente sul dispositivo (permettendo le app da origini sconosciute), ma non va sullo store.
