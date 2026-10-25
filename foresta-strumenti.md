# foresta-strumenti

Gli attrezzi di base che servono a tutto il resto del gioco: **il caso** (i dadi), **il registro** (il log), **il tempo** (il temporizzatore e il thread principale) e gli interruttori della **modalità di prova** per chi sviluppa.

Sta in fondo alla catena perché sono cose che chiunque può voler usare e che non hanno niente a che fare con le regole del gioco: nessuno dei suoi servizi sa che cosa sia un personaggio, una locazione o un evento. Tenerli in un modulo a sé, senza dipendenze, vuol dire che il compilatore impedisce a questi strumenti di imparare mai qualcosa del dominio.

## Dipendenze

**Dichiarate nel pom:** nessuna, solo il JDK.

**Lo dichiarano:** `foresta-eventi`, `foresta-testi`, `foresta-artefatti`, `foresta-mappa`, `foresta-personaggi`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

Non dipende da nessun modulo del progetto, nemmeno da `foresta-eventi`: il temporizzatore non pubblica eventi, segnala gli impulsi falliti a chi li ha chiesti, e `ThreadPrincipale` esiste proprio perché il bus degli eventi (che sta sopra) possa consegnare sul thread giusto senza conoscere Swing.

## Funzionamento

### Il caso: `strumenti.dado`

`Dado` è **l'unica sorgente di caso** del gioco: tutte le decisioni casuali (tiri, probabilità, scelte) passano da lì, e per questo una partita si può rendere ripetibile e i test possono decidere l'esito.

- **Il seme**: `impostaSeme(long)` fissa il generatore, e da quel momento i lanci non truccati sono ripetibili. Chi ha bisogno di un `Random` (il generatore di artefatti, le grammatiche) lo prende da `Dado.sorgente()`: segue il seme ma non consuma i lanci truccati, che restano riservati alle decisioni.
- **La coda dei trucchi**: `trucca(...)` mette in coda i prossimi risultati (un intero per ogni `tira…`, un `double` in [0, 1) per ogni `probabilita()`). Finiti i trucchi si torna al caso. Un risultato del tipo sbagliato o fuori intervallo è un errore, perché vuol dire che il codice lancia diversamente da come il test crede; i lanci senza scelta (una faccia sola) non consumano trucchi.
- **I lanci con contesto**: i lanci importanti dichiarano **per che cosa** si tira (`ContestoLancio`: la mossa dell'avversario a carta, forbici e sasso, il tipo di mostro, il numero di mostri, l'esito di un cofano, l'oggetto raro di un cofano). In modalità di prova un `TruccatoreDiDadi` installato con `Dado.impostaTruccatore` può decidere il risultato di un contesto, e il trucco resta finché non lo si spegne. La coda dei test ha la precedenza sul truccatore.

Un lancio non valido lancia un'eccezione il cui messaggio dice il metodo e i parametri. Il modulo `dado` non usa il `Logger`, per non dipendere dal resto.

### Il registro: `strumenti.logger`

`Logger` manda i messaggi alla `DestinazioneDiLog` installata. Quella predefinita del desktop, `DestinazioneConsoleEFile`, scrive sulla console e, se si passa `-Dforesta.log=cartella`, anche su un file per sessione (una sessione produce migliaia di righe, quindi conviene puntarla a un disco in RAM). Una piattaforma con un registro suo (Logcat, os_log) installa una destinazione propria con `Logger.impostaDestinazione`, prima del primo messaggio; `DestinazioneDiLog.NESSUNA` spegne tutto.

Un testo che costa costruirlo si passa come fornitore, `Logger.log(() -> "Colpo a " + nome)`: se la destinazione non scrive davvero (`isAttiva()` falso) il testo non viene nemmeno costruito. È una convenzione del progetto, e `ConvenzioniDiPortabilitaTest` la controlla.

### Il tempo: `strumenti.temporizzatore`

- **`Temporizzatore` e `Temporizzabile`**: il primo manda un impulso (`Temporizzabile.tick()`) a intervalli regolari a chi glielo ha chiesto (`setTemporizzabile`). Serve per i round di combattimento, l'intro, la fine partita e le animazioni. `inizia(ms)` fa partire il primo impulso subito, `iniziaDopo(ms)` dopo un intervallo; `termina()` ferma tutto per sempre, mentre `sospendi()` ferma gli impulsi senza dimenticare il ritmo e `riprendi()` li fa ripartire (serve quando l'app passa in background: un combattimento non avanza e un'animazione non scorre). `FRAME_PER_SECONDO` (60) è il ritmo delle animazioni, che avanzano in base al tempo reale e non per fotogramma.
- **`TemporizzatoreJ2SE`**: l'implementazione sull'esecutore pianificato del JDK. Gli impulsi falliti si segnalano a chi li ha chiesti.
- **`ThreadPrincipale`**: il thread su cui gira **tutta la logica di gioco**. Il motore e il bus eventi non conoscono il toolkit grafico: chiedono qui di eseguire un'operazione sul thread principale (`esegui` subito se si è già lì, `accoda` dopo), e chi assembla il gioco installa l'`Esecutore` del proprio toolkit (con Swing è `ui/EsecutoreSwing`, installato da `Main`). Finché non se ne installa uno le operazioni girano subito sul thread chiamante: è così nei test.

### La modalità di prova: `ModalitaDiProva` e `TrucchiDiSviluppo`

`ModalitaDiProva` è un semplice interruttore, acceso dall'argomento di avvio `MODALITA_DI_PROVA` (vedi `Main`). Con la modalità accesa il gioco regala monete, pergamene e pozioni a volontà, svela la mappa e mette in campo artefatti potenti; senza, la partita parte come per un giocatore vero.

`TrucchiDiSviluppo` è il `TruccatoreDiDadi` della finestra di prova: per ogni `ContestoLancio` tiene **un trucco permanente** con il numero voluto (o l'elemento voluto, per i contesti che scelgono). Un trucco il cui valore non è fra quelli possibili in quel lancio (un 5 per un lancio da 1 a 3) non si applica e quel lancio va al caso. Lo stato può essere letto e scritto da qualunque thread. Il resto è nella [guida agli strumenti di prova](strumenti_di_prova.md).

## Osservazioni

- Tutti i servizi sono statici (`Dado`, `Logger`, `ThreadPrincipale`, `ModalitaDiProva`): comodi, ma sono stato globale, e i test devono ripulire dopo di sé (per esempio riportare il seme e i trucchi). È coerente con il resto del gioco, che è single-player.
- Il modulo non apre mai finestre né chiude il processo: i front-end diversi da Swing hanno solo da installare il proprio `Esecutore` e la propria `DestinazioneDiLog`.
