# 13-foresta-app-libgdx

L'**applicazione desktop** (Windows, Linux, macOS) con l'interfaccia libGDX: il punto d'ingresso (`MainLibgdx`) che collega il motore, come `Main` per Swing, e apre la finestra con il backend LWJGL3. Android e iOS hanno il loro punto d'ingresso, fuori da Maven (vedi [`porting_libGDX.md`](porting_libGDX.md)).

## Dipendenze

**Dichiarate nel pom:** `12-foresta-motore`, `12-foresta-libgdx`, `com.badlogicgames.gdx:gdx-backend-lwjgl3` e `gdx-platform` (classificatore `natives-desktop`).

**Lo dichiarano:** nessuno.

## Funzionamento

`MainLibgdx` legge gli argomenti con `OpzioniDiAvvio` (modulo comune, la stessa di Swing; contano tutte: `ORIZZONTALE`/`VERTICALE`, `TUTTOSCHERMO`, `SALTALOGO`, `BARRACLASSICA`, `JOYSTICK_VIRTUALE`, `LINGUA=`, `ZOOM=` e `MODALITA_DI_PROVA`) e collega il motore con `AvvioDelMotore` (in `12-foresta-motore`, lo stesso di Swing); il thread principale è quello di rendering di libGDX (`EsecutoreGdx`, che tiene in attesa il lavoro accodato prima che l'applicazione parta) e `NotificaUscitaDalGioco` chiude l'applicazione con `Gdx.app.exit()`. L'interfaccia è `ForestaLibGDX`, che riceve l'automa (per il ciclo di vita), la classifica, la `VistaPartitaMotore` e il temporizzatore della UI; con l'argomento `PAGINE_DI_PROVA` parte invece `ForestaGdx`, le pagine di prova senza il motore. Poi configura la finestra (titolo «La Foresta», le dimensioni dello schermo di gioco, 1022×804 a zoom 2, o 1142×656 per le pagine di prova, vsync, 60 fotogrammi al secondo) e la avvia. Su macOS la finestra deve stare sul primo thread del processo (`-XstartOnFirstThread`): se la JVM non è partita così, `MainLibgdx` la fa ripartire con l'opzione (proprietà `foresta.riavviata`), senza chiudere il processo con `System.exit`.

`mvn package` scrive nella radice `foresta-libgdx-0.0.1-SNAPSHOT.jar` e `foresta-libgdx-0.0.1-SNAPSHOT-jar-with-dependencies.jar` (che non si committano, vedi `.gitignore`); si lancia con `java -jar foresta-libgdx-0.0.1-SNAPSHOT-jar-with-dependencies.jar`.

## Osservazioni

- Lo `SnifferBusEventi` (il registro di tutti gli eventi) sta in `13-foresta-app-swing` e conosce gli eventi con gli sprite di Swing: qui non c'è.

## Da fare / idee aperte

- La chiusura della finestra non passa dalla conferma di uscita (in Swing nemmeno: `EXIT_ON_CLOSE`).
- `TUTTOSCHERMO` usa la risoluzione del desktop con `setFullscreenMode`; il gioco resta alla sua risoluzione virtuale, adattata dal `FitViewport` con le bande nere (in Swing la finestra prende invece la dimensione dello schermo e il layout la segue).
