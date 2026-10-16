# Assessment: tipi, modello dati e portabilità verso Android e iOS

Stato al 2026-10-16. Riguarda i moduli `foresta-tipi`, `foresta-modellodati` e `foresta-contratti`, più le parti di `foresta-motore` che toccano disco e thread. `foresta-swing` e la logica del motore non sono ancora stati esaminati.

In sintesi: i moduli bassi sono già in buona forma per un porting. Restano 17 punti circoscritti, elencati qui sotto con un codice (P = portabilità, Q = qualità del codice) per affrontarli uno alla volta.

## I punti in breve

| ID | Punto | Dove | Priorità | Fatto |
| :--- | :--- | :--- | :--- | :--- |
| P1 | Cartella dei salvataggi presa da `user.home` | `salvataggi/GestoreSuFile.java:19` | Alta | [x] |
| P3 | `Logger` apre un `File` in un inizializzatore statico | `strumenti/Logger.java:25,40` | Alta | [x] |
| P6 | `String.format` senza `Locale` | `ModificatoreAttributoMD.java:160-179`, `TipoMotivoRifiutoEquipaggiamento.java:79,93` | Alta | [x] |
| Q1 | `HashMap` con chiavi enum invece di `EnumMap` | `PersonaggioMD` | Alta | [x] |
| Q2 | Si toglie solo il "\|": un a capo rompe il formato a righe | `Serializzabile.pulisciCampo`, `MessaggioMD.salva` | Alta | [x] |
| P4 | Stato globale statico e morte del processo su Android | `ModelloDati.getIstanza()` (64 file), `BusEventi`, `Dado`, `ForestaMD` | Media | [ ] |
| P5 | Thread del motore da legare al ciclo di vita dell'app | `TemporizzatoreJ2SE`, `Automa.java:340` | Media | [ ] |
| Q3 | Lettura del salvataggio non uniforme | `LocazioneMD`, `MissioneMD`, `PersonaggioMD` | Media | [x] |
| Q5 | Collezioni interne esposte da getter e setter | `PersonaggioMD` | Media | [ ] |
| Q10 | Test di andata e ritorno nel modulo stesso | `foresta-modellodati/src/test` | Media | [ ] |
| P2 | `java.nio.file` nei gestori su file | `GestoreTrofeiSuFile`, `GestoreSalvataggiSuFile` | Bassa | [x] |
| P7 | Testi italiani dentro tipi e costanti | `Comando`, `TipoMotivoRifiutoEquipaggiamento`, `Costanti` | Bassa (decisione) | [ ] |
| Q4 | Circa 400 righe di scorciatoie, con `int` e `double` misti | `PersonaggioMD` | Bassa | [x] |
| Q6 | Testo e cache di disegno nel modello dati | `ModificatoreAttributoMD.descrizione*()`, `ForestaMD.versioneMappa` | Bassa | [ ] |
| Q7 | "vivo" come valore sentinella della causa di morte | `PersonaggioMD.java:741` | Bassa | [ ] |
| Q8 | 2077 costanti di bilanciamento scritte come codice | `Costanti` | Bassa | [ ] |
| Q9 | Stato mutabile negli enum | `TipoPersonaggio.dannoSubito` | Bassa | [ ] |

## Cosa aiuta già il porting

- **Niente riflessione** nel codice di produzione (niente `Class.forName`, `getDeclared*`, `ServiceLoader`, proxy): importante per gli strumenti che compilano Java in nativo per iOS (GraalVM/Gluon, MobiVM, J2ObjC).
- `tipi`, `modellodati` e `contratti` usano solo `java.util`, `java.io` (reader e writer) e `java.util.concurrent`.
- Moduli separati dal compilatore; la UI legge da `VistaPartita` e manda solo eventi; `DipendenzeDalToolkitGraficoTest` tiene AWT e Swing dentro `foresta-swing`.
- `ThreadPrincipale` astrae il thread principale (main looper su Android, coda principale su iOS).
- `Dado` è l'unica sorgente di caso.
- I salvataggi sono testo UTF-8 scritto con `Double.toString`/`parseDouble`: non dipendono dalla lingua del dispositivo.

## Portabilità (P1–P7)

### P1 – Cartella dei salvataggi

`GestoreSuFile.recuperaDirectory()` costruisce la cartella da `System.getProperty("user.home")`. Su Android serve `Context.getFilesDir()`, su iOS la cartella Documents o Application Support.

- [x] Fatto: `GestoreSuFile` ha un costruttore che prende la cartella, e così i tre gestori (`GestoreSalvataggiSuFile`, `GestorePunteggiSuFile`, `GestoreTrofeiSuFile`). Senza argomenti scrivono in `~/.foresta` come prima, quindi `Main` non cambia. Su mobile la piattaforma passa la sua cartella privata. Coperto da `GestoreTrofeiSuFileTest`.

### P2 – `java.nio.file`

`GestoreTrofeiSuFile` e `GestoreSalvataggiSuFile` (scrittura atomica con file temporaneo) usano `Files`, `Path` e `StandardCopyOption`. Su Android richiedono minSdk 26.

- [x] Fatto: solo `java.io.File`, niente più `java.nio.file` nel codice di produzione (resta `java.nio.charset.StandardCharsets`, disponibile su Android da sempre). I gestori di trofei e classifica usano `FileInputStream` e `FileOutputStream`; `GestoreSalvataggiSuFile` sostituisce il file con `File.renameTo` e, dove non basta (Windows), toglie prima il vecchio. Coperto da `GestoreSalvataggiSuFileTest`, che non rilegge il salvataggio perché lo farebbe col modello dati statico.
- Effetto collaterale trovato: `PartitaDiTest.close()` lasciava accesa la modalità di prova (ripristinava solo se prima era accesa), e `BugMappaReimpostataTest` falliva se gli passava davanti un test di partita. Corretto: ora ripristina il valore di prima.

### P3 – `Logger`

Il `PrintWriter` del log si apre in un inizializzatore statico, da una proprietà di sistema, con `File` e `SimpleDateFormat`.

- [x] Fatto: nuova interfaccia `DestinazioneDiLog` e `Logger.impostaDestinazione(...)`. Il comportamento del desktop (console, più file con `-Dforesta.log`) è in `DestinazioneConsoleEFile`, creata al primo messaggio e non più all'avvio della classe. Una piattaforma installa la sua (Logcat, `os_log`) prima del primo messaggio. Coperto da `LoggerTest`.

### P4 – Stato globale statico

`ModelloDati.getIstanza()` è usato in 64 file del motore; `BusEventi` e `Dado` sono statici; `ForestaMD` ha un contatore statico (`ULTIMA_VERSIONE_MAPPA`). Su Android il sistema può uccidere il processo in background: lo stato statico sparisce senza preavviso.

- [ ] Un unico punto che crea, azzera e ricarica tutto lo stato.
- [ ] Salvataggio automatico quando l'app va in background (`onPause` su Android, `sceneDidEnterBackground` su iOS).

### P5 – Thread del motore

`TemporizzatoreJ2SE` usa uno `ScheduledExecutorService`; `Automa` avvia un thread di precaricamento (`Automa.java:340`). Funzionano anche su mobile, ma vanno fermati e ripresi con l'app.

- [ ] Metterli dietro un'interfaccia con sospendi/riprendi.

### P6 – `String.format` senza `Locale`

Con un telefono in arabo, persiano o hindi `%+d` produce cifre non latine, che il font bitmap Doomdark non sa disegnare.

- [x] Fatto: `Locale.ROOT` in tutti i `String.format` con numeri che finiscono in un testo mostrato: `ModificatoreAttributoMD` (4), `SpriteRivelazioneArtefatto` (2), `Ordinamenti` (1) e `SceltaIncantesimo` (4, descrizioni delle tattiche). `TipoMotivoRifiutoEquipaggiamento` e `Mandante` formattano solo `%s`, quindi non dipendono dalla lingua e non sono stati toccati. Coperto da `ModificatoreAttributoMDTest`, che mette il telefono in arabo.
- [ ] Restano fuori, per scelta: i `String.format` dei log (`CalcolatoreCombattimento`, `SnifferBusEventi`), che non si vedono. Restano da guardare i `toLowerCase()`/`toUpperCase()` senza `Locale`: con un telefono in turco "I" diventa "ı" e il font bitmap non lo trova. Quelli che alimentano il font sono `TestoGrande`, `SpriteAnnuncioGlobale` e `DisplayableCanvasIntroOutro`.

### P7 – Testi italiani nel codice

Le descrizioni di `Comando`, le spiegazioni dei rifiuti e 725 note `…_NOTA` in `Costanti` sono prosa italiana dentro il codice. Se il gioco resta solo in italiano va bene.

- [ ] Decidere se serve l'inglese per gli store; se sì, spostare i testi in risorse partendo da `Comando` e `Costanti`.

## Qualità del codice (Q1–Q10)

### Q1 – `EnumMap` per le chiavi enum

`PersonaggioMD` tiene `valoriMinimi`, `valoriMassimi` e `valoriAttributi` in `HashMap<TipoAttributo, Double>`. L'ordine di scrittura nel salvataggio segue gli hash d'identità, quindi due salvataggi della stessa partita possono differire riga per riga.

- [x] Fatto: le tre mappe di `PersonaggioMD` sono `EnumMap`, quindi gli attributi si scrivono sempre nell'ordine dell'enum. Le altre mappe con chiavi enum nel modello dati lo erano già; quelle con chiavi stringa (`proprieta`, registri) hanno un ordine di hash deterministico. Coperto da `TestiNeiSalvataggiTest`, che con la vecchia `HashMap` fallisce.

### Q2 – Pulizia dei testi salvati

`Serializzabile.senzaPipe` toglie solo il "|"; `MessaggioMD.salva` non pulisce nemmeno quello (rilegge il testo fino a fine riga). Un a capo in un nome, una nota o un messaggio rompe il formato a righe; con la tastiera di un telefono è più facile che succeda.

- [x] Fatto: `senzaPipe` è diventata `Serializzabile.pulisciCampo`, che toglie il "|" e trasforma gli a capo (`\n`, `\r`, `\r\n`) in uno spazio; la usano gli stessi setter di prima (nomi, descrizioni, note, causa di morte, proprietà, notizie, residenti, nome delle voci di tattica, classifica, `Prompt`). `MessaggioMD.salva` usa `senzaACapo`, che lascia il "|" (il testo è l'ultimo campo). Una chiave di proprietà con a capo è rifiutata come quelle con "|" o "§". Coperto da `TestiNeiSalvataggiTest`.

### Q3 – Lettura del salvataggio non uniforme

`LocazioneMD` e `MissioneMD` usano ancora `line.split("\\|", -1)` invece di `LettoreCampi`; `PersonaggioMD` divide i campi composti a mano con `split(MappaProprieta.SEPARATORE)`.

- [x] Fatto: `LocazioneMD`, `MissioneMD`, `MessaggioMD` e `PersonaggioMD` leggono solo con `LettoreCampi`. Due metodi nuovi: `sottocampi(minimo, massimo)` per i campi composti con "§" (attributi, modificatori, effetti di stato; l'ultimo sottocampo, la nota, tiene il resto) e `resto()` per l'ultimo campo che può contenere il "|" (il testo di un messaggio). `MappaProprieta.leggi` lavora ora su un `LettoreCampi`. Un salvataggio troncato o rovinato dà sempre un `IOException` che dice cosa manca; prima `MissioneMD` e `MessaggioMD` davano `NullPointerException` o indice fuori dai limiti. Un messaggio con tipo diverso da P o F è rifiutato. Coperto da `LetturaDeiSalvataggiTest`.

### Q4 – Scorciatoie di `PersonaggioMD`

803 righe, di cui circa 400 di `getForza`/`setForzaMassima`… scritte a mano. Alcuni setter prendono `int` (`setDestrezza`, `setCostituzione`, `setMagia`, `setNumeroBersagli`), gli altri `double`.

- [x] Fatto, a metà strada: ho tolto le 60 scorciatoie che non chiamava nessuno (tutte le `…Massimo`/`…Massima`, `getNumeroBersagli`, i setter degli attributi secondari…), circa 180 righe, e ho reso `double` il parametro di tutti i setter rimasti (`setDestrezza`, `setCostituzione`, `setMagia` prendevano `int`). Restano le 33 che il compilatore dice usate: i setter dei sei attributi primari e della fortuna (40 classi di personaggio le chiamano nel costruttore), i getter/setter che `PersonaggioBase` usa uno per uno, e `getCaricoMassimo`, che ha una regola sua (0 se manca il massimo).
- [ ] Resta da decidere se togliere anche quelle: i 25 getter sono chiamati solo da `PersonaggioBase`, che potrebbe usare `get(TipoAttributo.X)` con il taglio a intero dichiarato lì; i 7 setter sono letti bene nei costruttori delle 40 classi e conviene tenerli.

### Q5 – Collezioni interne esposte

`getArtefatti()`, `getModificatori()` e `getEffettiDiStato()` restituiscono le liste vere; i `set…(Collection)` le sostituiscono con quella del chiamante, che resta condivisa.

- [ ] Copie difensive nei setter e viste non modificabili (o metodi `aggiungi…`/`togli…`) nei getter.

### Q6 – Presentazione nel modello dati

`ModificatoreAttributoMD.descrizione*()` ("per 3 turni", "sul bersaglio") e `ForestaMD.versioneMappa`, che serve solo alla cache del disegno, contraddicono il "solo dati" del `package-info`.

- [ ] Spostare le descrizioni nel motore o nella UI; tenere la versione della mappa fuori dal modello dati.

### Q7 – Sentinella "vivo"

`PersonaggioMD.salva` scrive "vivo" nel campo della causa di morte quando il personaggio è vivo.

- [ ] Un campo booleano separato per vivo/morto.

### Q8 – `Costanti`

2335 righe, 2077 costanti; le tabelle per classe (`GOBLIN_MAX_FORZA`…) sono dati. I `static final` primitivi vengono copiati nelle classi che li usano, quindi una compilazione incrementale parziale può tenere valori vecchi.

- [ ] Valutare una risorsa (properties o JSON) per le tabelle per classe e le note: bilanciamento senza ricompilare, note traducibili (vedi P7).

### Q9 – Stato mutabile negli enum

`TipoPersonaggio.dannoSubito` è una mappa riempita in un blocco `static`.

- [ ] Costruire la tabella una volta e renderla immutabile, o tenerla fuori dall'enum.

### Q10 – Test del modello dati

In `foresta-modellodati` ci sono test solo per Foresta, Intermezzi, LettoreCampi, Notizie e Trofei; i test di andata e ritorno di `PersonaggioMD`, `ArtefattoMD` e `ModelloDati` stanno in `foresta-motore`.

- [ ] Un test di andata e ritorno nel modulo stesso, con nomi che contengono "|", "§" e a capo.

## Osservazioni

- Il grosso del porting resta la UI Swing, da riscrivere, e l'input. I `Comando` astratti aiutano, ma i cartigli al passaggio del mouse e le scorciatoie da tastiera vanno ripensati per il tocco.
- Per un gioco 2D che resta in Java la scelta più naturale è **libGDX**: un solo codice di UI per desktop, Android e iOS (tramite MobiVM), che può usare direttamente i jar di motore e contratti. L'alternativa è J2ObjC per il motore, con una UI nativa separata per ciascuna piattaforma.

## Da fare / idee aperte

- Esaminare `foresta-swing` (dimensioni dello schermo, input, immagini) e la logica del motore con lo stesso criterio.
