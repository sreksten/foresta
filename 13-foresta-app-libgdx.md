# 13-foresta-app-libgdx

L'**applicazione desktop** (Windows, Linux, macOS) con l'interfaccia libGDX: il punto d'ingresso (`MainLibgdx`) che apre la finestra con il backend LWJGL3. Android e iOS hanno il loro punto d'ingresso, fuori da Maven (vedi [`porting_libGDX.md`](porting_libGDX.md)).

## Dipendenze

**Dichiarate nel pom:** `12-foresta-libgdx`, `com.badlogicgames.gdx:gdx-backend-lwjgl3` e `gdx-platform` (classificatore `natives-desktop`).

**Lo dichiarano:** nessuno.

## Funzionamento

`MainLibgdx` configura la finestra (titolo «La Foresta», 1142×656, vsync, 60 fotogrammi al secondo) e avvia `ForestaGdx`. Su macOS la finestra deve stare sul primo thread del processo (`-XstartOnFirstThread`): se la JVM non è partita così, `MainLibgdx` la fa ripartire con l'opzione (proprietà `foresta.riavviata`), senza chiudere il processo con `System.exit`.

`mvn package` scrive nella radice `foresta-libgdx-0.0.1-SNAPSHOT.jar` e `foresta-libgdx-0.0.1-SNAPSHOT-jar-with-dependencies.jar` (che non si committano, vedi `.gitignore`); si lancia con `java -jar foresta-libgdx-0.0.1-SNAPSHOT-jar-with-dependencies.jar`.

## Osservazioni

- Per ora non collega il motore: mostra solo la finestra e gli atlanti.

## Da fare / idee aperte

- Collegare il motore come in `13-foresta-app-swing` (`Main`: modello dati, registrazione dei componenti, thread principale su `Gdx.app.postRunnable`).
