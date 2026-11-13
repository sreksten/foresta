# 13-foresta-app-android

L'**APK di debug per Android**, costruito da Maven insieme ai jar: il modulo non ha codice, copia i jar del gioco appena compilati e lancia il progetto Gradle [`foresta-android/`](foresta-android.md) (con `AndroidLauncher`, il manifest e le icone), poi mette l'APK nella radice. Entra nel build solo con il profilo `android`: `mvn package -Pandroid`.

## Dipendenze

**Dichiarate nel pom:** `12-foresta-motore`, `12-foresta-libgdx`.

**Lo dichiarano:** nessuno (lo elenca solo il profilo `android` del pom padre).

## Funzionamento

È un modulo `pom` che lavora tutto nella fase `package`, in quest'ordine:

1. `maven-dependency-plugin:copy-dependencies` copia in `target/libs` i jar del gioco appena compilati (i 30 moduli sotto il motore e l'interfaccia libGDX), non quelli di `~/.m2`: non serve più `mvn install`. libGDX resta fuori (`excludeGroupIds`): Gradle lo prende da Maven Central con `gdx-backend-android`, e due copie di `gdx` darebbero classi doppie al momento di fare il dex.
2. `exec-maven-plugin` lancia `foresta-android/gradlew assembleDebug -Pforesta.libs=<target/libs>` (`gradlew.bat` su Windows, con il profilo `windows` del modulo) con `JAVA_HOME` uguale alla proprietà `android.java.home`.
3. `maven-antrun-plugin` copia `foresta-android/app/build/outputs/apk/debug/app-debug.apk` nella radice come `foresta-android-0.0.1-SNAPSHOT.apk`, accanto ai jar; si committa come i jar di Swing.

```
mvn package -Pandroid                                         # Maven gira già con un JDK 17-21
mvn package -Pandroid -Dandroid.java.home=/usr/lib/jvm/java-21-openjdk-amd64
```

Servono:

- **l'SDK Android** (piattaforma 36, build-tools), con `ANDROID_HOME` o `sdk.dir` in `foresta-android/local.properties`; senza, Gradle si ferma con «SDK location not found»;
- **un JDK dal 17 al 21 per Gradle** (l'Android Gradle Plugin 8.13 non va sotto il 17 e non vuole il 22): è quello di Maven, se non si passa `-Dandroid.java.home`.

Senza il profilo `mvn package` non tocca Android e non ha bisogno di niente di questo.

## Osservazioni

- L'APK è di **debug**, firmato con la chiave di debug dell'SDK: si installa con `adb install -r` o aprendolo sul dispositivo (con «origini sconosciute» permesse), ma non va sullo store.
- Gradle allunga il build di parecchio (la prima volta scarica l'Android Gradle Plugin e libGDX): è uno dei motivi per tenerlo in un profilo.
- `--no-daemon`: il demone di Gradle non resta acceso dopo il build di Maven.
- Gradle si può usare anche da solo dentro `foresta-android/`: senza `-Pforesta.libs` cerca i jar in `13-foresta-app-android/target/libs`, cioè dove li ha lasciati l'ultimo `mvn package -Pandroid`.
- I test (`mvn test -Pandroid`) non cambiano: il modulo non ne ha.

## Da fare / idee aperte

- L'APK di release firmato (keystore e password fuori dal repository, per esempio in `~/.gradle/gradle.properties`) e il pacchetto per lo store (`.aab`, `bundleRelease`).
- Il numero di versione dell'APK (`versionName`, `versionCode` in `app/build.gradle`) potrebbe venire dal pom invece di essere scritto a mano.
